package jp.ac.osakau.farseerfc.purano.reflect;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import jp.ac.osakau.farseerfc.purano.dep.DepEffect;
import jp.ac.osakau.farseerfc.purano.dep.DepInterpreter;
import jp.ac.osakau.farseerfc.purano.dep.DepValue;
import jp.ac.osakau.farseerfc.purano.table.TypeNameTable;
import jp.ac.osakau.farseerfc.purano.table.Types;
import lombok.Getter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;


public class MethodRep extends MethodVisitor {
	private final @Getter MethodInsnNode node;
	private final @Getter Method reflect;
	private final @Getter List<MethodRep> overrides = new ArrayList<>();
	private final @Getter List<MethodInsnNode> calls = new ArrayList<>();
	
	private int timeStamp;
	private @Getter DepEffect effects;
	private @Getter MethodNode methodNode;
	
	public MethodRep(MethodInsnNode methodNode){
		super(Opcodes.ASM4);
		this.node = methodNode;
		if(node.name.equals("<init>")){
			this.reflect = null;
		}else{
			this.reflect = getReflectFromNode(methodNode);
		}
		
		resolve(0);
	}
	
	public MethodRep(Method reflect,String owner){
		super(Opcodes.ASM4);
		this.reflect = reflect;
		this.node = new MethodInsnNode(0,
				owner,
				reflect.getName(), 
				Type.getMethodDescriptor(reflect));
		resolve(0);
	}
	
	private static final Map<String, Class<?>> primitiveClasses = new HashMap<String, Class<?>>();

	static {
	    primitiveClasses.put("byte", byte.class);
	    primitiveClasses.put("short", short.class);
	    primitiveClasses.put("char", char.class);
	    primitiveClasses.put("int", int.class);
	    primitiveClasses.put("long", long.class);
	    primitiveClasses.put("float", float.class);
	    primitiveClasses.put("double", double.class);
	}

	public static Function<Type, Class<? extends Object>> loadClass = new Function<Type, Class<? extends Object>> (){
		@Nullable @Override
		public Class<? extends Object> apply(Type t){
			String name = t.getClassName();
			if(name.endsWith("[]")){
				name = Types.binaryName2NormalName(t.getInternalName());
			}
			//System.err.println(String.format("ArgumentTypes: %s",name));
		    if (primitiveClasses.containsKey(name)) {
		        return primitiveClasses.get(name);
		    } else {
		        try {
					return Class.forName(name);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
		    }
		}
	};

	private Method getReflectFromNode(MethodInsnNode node) {
		try {
			Class<? extends Object> cls = Class.forName(Types.binaryName2NormalName(node.owner));
			try{
				// Try to get the declared method, which may be private, that declared in this class
				return cls.getDeclaredMethod(
						node.name,
						Lists.transform(
								Lists.newArrayList(
										Type.getType(node.desc).getArgumentTypes()),
								loadClass).toArray(new Class[0]));
			}catch(NoSuchMethodException e){
				// Try to get the method that may be declared in superclass, must be public 
				return cls.getMethod(
						node.name,
						Lists.transform(
								Lists.newArrayList(
										Type.getType(node.desc).getArgumentTypes()),
								loadClass).toArray(new Class[0]));
			}
		} catch (NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getId(){
		return node.name+node.desc;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(reflect, node.desc, node.name,node.owner);
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof MethodRep){
			return this.equals(other);
		}
		return false;
	}
	

	public String toString(TypeNameTable table){
		return String.format("%s",
				table.dumpMethodDesc(node.desc, 
						String.format("%s#%s", 
								table.fullClassName(node.owner),
								node.name)));
	}
	
	public boolean equals(MethodRep other){
		if (!Objects.equal(this.reflect, other.reflect))
			return false;
		if (!Objects.equal(this.node.desc, other.node.desc))
			return false;
		if (!Objects.equal(this.node.name, other.node.name))
			return false;
		if (!Objects.equal(this.node.owner, other.node.owner))
			return false;
		return true;
	}
	
	public List<String> dump(TypeNameTable table){
		List<String> result = new ArrayList<>();
		result.add("    "+toString(table));
		for(MethodRep rep : overrides){
			result.add(String.format("        @ %s", rep.toString(table)));
		}
		for(MethodInsnNode insn : calls){
			result.add(String.format("        > %s", new MethodRep(insn).toString(table)));
		}
		result.add(effects.dump(getMethodNode(), table));
		return result;
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		calls.add(new MethodInsnNode(opcode,owner,name,desc));
	}
	
	public boolean resolve(int newTimeStamp){
		try {
			ClassReader cr=new ClassReader(node.owner);
			cr.accept(new ClassVisitor(Opcodes.ASM4){
				@Override
				public MethodVisitor visitMethod(int access, String name,
						String desc, String signature, String[] exceptions) {
					if(!node.name.equals(name)||!node.desc.equals(desc)){
						return null;
					}
					return new TraceMethodVisitor(new MethodNode(Opcodes.ASM4,access,name,desc,signature,exceptions){
						@Override
						public void visitEnd() {
							super.visitEnd();
							//printf("}\n{\n");
							effects = new DepEffect();
							Analyzer<DepValue> ana = new Analyzer<DepValue>(new DepInterpreter(effects));
							try {
								/*Frame<DepValue> [] frames =*/ ana.analyze("dep", this);
								methodNode = this;
							} catch (AnalyzerException e) {
								e.printStackTrace();
							}
							//printf("%s\n",effect.dump(this,new TypeNameTable()));
							//printf("}\n");
						}
					},new Textifier(Opcodes.ASM4){
						@Override public void visitMethodEnd() {
							//print(writer); 
						}
					});
				}
			}, 0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return true;
	}
}
