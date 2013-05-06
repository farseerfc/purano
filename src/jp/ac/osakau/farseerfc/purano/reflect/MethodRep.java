package jp.ac.osakau.farseerfc.purano.reflect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ac.osakau.farseerfc.purano.dep.DepEffect;
import jp.ac.osakau.farseerfc.purano.dep.DepInterpreter;
import jp.ac.osakau.farseerfc.purano.dep.DepValue;
import jp.ac.osakau.farseerfc.purano.util.MethodDesc;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Getter;
import lombok.Setter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import com.google.common.base.Objects;


public class MethodRep extends MethodVisitor {
	private final @Getter MethodInsnNode insnNode;
	//private final @Getter Method reflect;
	private final @Getter List<MethodRep> overrides = new ArrayList<>();
	private final @Getter List<MethodInsnNode> calls = new ArrayList<>();
	
	private @Getter boolean isStatic ;
	private final @Getter MethodDesc desc ;
	
	private int timeStamp;
	private @Getter DepEffect staticEffects;
	private @Getter DepEffect dynamicEffects;
	private @Getter @Setter MethodNode methodNode;
	
	public MethodRep(MethodInsnNode methodInsnNode){
		super(Opcodes.ASM4);
		this.insnNode = methodInsnNode;

		desc=new Types(false).method2full(methodInsnNode.desc);
	}
	
	public String getId(){
		return insnNode.name+insnNode.desc;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(insnNode.desc, insnNode.name,insnNode.owner);
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof MethodRep){
			return this.equals(other);
		}
		return false;
	}
	

	public String toString(Types table){
		return toString(insnNode,table);
	}
	
	public String toString(MethodInsnNode node, Types table){
		return String.format("%s",
				table.dumpMethodDesc(node.desc, 
						String.format("%s#%s", 
								table.fullClassName(node.owner),
								node.name)));
	}
	
	public boolean equals(MethodRep other){
		if (!Objects.equal(this.insnNode.desc, other.insnNode.desc))
			return false;
		if (!Objects.equal(this.insnNode.name, other.insnNode.name))
			return false;
		if (!Objects.equal(this.insnNode.owner, other.insnNode.owner))
			return false;
		return true;
	}
	
	public List<String> dump(Types table){
		List<String> result = new ArrayList<>();
		result.add("    "+toString(table));
		for(MethodRep rep : overrides){
			result.add(String.format("        @ %s", rep.toString(table)));
		}
		for(MethodInsnNode insn : calls){
			result.add(String.format("        > %s", toString(insn,table)));
		}
		
		if(staticEffects != null && getMethodNode() != null){
			result.add(staticEffects.dump(this, table,"            "));
		}
		return result;
	}
	
	
	public int argCount(){
		MethodDesc p = new Types().method2full(insnNode.desc);
		if (((methodNode.access & Opcodes.ACC_STATIC) == 0)) {
			return p.getArguments().size() + 1; // for this
		} else {
			return p.getArguments().size();
		}
	}
	
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		calls.add(new MethodInsnNode(opcode,owner,name,desc));
	}
	
	public boolean resolve(int newTimeStamp){
		try {
			ClassReader cr=new ClassReader(insnNode.owner);
			final MethodRep thisRep = this;
			cr.accept(new ClassVisitor(Opcodes.ASM4){
				@Override
				public MethodVisitor visitMethod(int access, String name,
						String desc, String signature, String[] exceptions) {
					if(!insnNode.name.equals(name)||!insnNode.desc.equals(desc)){
						return null;
					}
					
					isStatic = (access & Opcodes.ACC_STATIC) > 0;
					
					return new MethodNode(Opcodes.ASM4,access,name,desc,signature,exceptions){
						@Override
						public void visitEnd() {
							super.visitEnd();
							methodNode = this;
							staticEffects = new DepEffect();
							Analyzer<DepValue> ana = new Analyzer<DepValue>(new DepInterpreter(staticEffects, thisRep));
							try {
								/*Frame<DepValue> [] frames =*/ ana.analyze("dep", this);
							} catch (AnalyzerException e) {
								e.printStackTrace();
							}
						}
					};
				}
			}, 0);
		} catch (IOException e) {
			throw new RuntimeException("Class not found :"+insnNode.owner,e);
		}
		return true;
	}
}
