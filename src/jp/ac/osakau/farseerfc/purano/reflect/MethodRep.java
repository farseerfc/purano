package jp.ac.osakau.farseerfc.purano.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import jp.ac.osakau.farseerfc.purano.table.Types;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import lombok.Getter;


public class MethodRep extends MethodVisitor {
	private final @Getter MethodInsnNode node;
	private final @Getter Method reflect;
	private final @Getter List<MethodRep> overrides = new ArrayList<>();
	private final @Getter List<MethodInsnNode> calls = new ArrayList<>();
	
	public MethodRep(MethodInsnNode methodNode){
		super(Opcodes.ASM4);
		this.node = methodNode;
		if(node.name.equals("<init>")){
			this.reflect = null;
		}else{
			this.reflect = getReflectFromNode(methodNode);
		}
	}
	
	public MethodRep(Method reflect,String owner){
		super(Opcodes.ASM4);
		this.reflect = reflect;
		this.node = new MethodInsnNode(0,
				owner,
				reflect.getName(), 
				Type.getMethodDescriptor(reflect));
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
	
	@Override
	public String toString(){
		return String.format("%s %s %s", node.name,node.owner,node.desc);
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
	
	public List<String> dump(){
		List<String> result = new ArrayList<>();
		result.add("    "+toString());
		for(MethodRep rep : overrides){
			result.add(String.format("        ✍ %s", rep));
		}
		for(MethodInsnNode insn : calls){
			result.add(String.format("        ☏ %s", new MethodRep(insn)));
		}
		return result;
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		calls.add(new MethodInsnNode(opcode,owner,name,desc));
	}
}
