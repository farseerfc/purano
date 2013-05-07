package jp.ac.osakau.farseerfc.purano.reflect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ac.osakau.farseerfc.purano.dep.DepEffect;
import jp.ac.osakau.farseerfc.purano.dep.DepInterpreter;
import jp.ac.osakau.farseerfc.purano.dep.DepValue;
import jp.ac.osakau.farseerfc.purano.effect.NativeEffect;
import jp.ac.osakau.farseerfc.purano.util.MethodDesc;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;


public class MethodRep extends MethodVisitor {
	private static final Logger log = LoggerFactory.getLogger(MethodRep.class);
	
	private final @Getter MethodInsnNode insnNode;
	//private final @Getter Method reflect;
	private final @Getter List<MethodRep> overrides = new ArrayList<>();
	private final @Getter List<MethodInsnNode> calls = new ArrayList<>();
	
	private final @Getter MethodDesc desc ;
	private final @Getter boolean isStatic ;
	private final @Getter boolean isNative;
	private final @Getter boolean isAbstract;
	
	private @Getter int modifiedTimeStamp;
	private @Getter int resolveTimeStamp;
	private @Getter DepEffect staticEffects;
	private @Getter DepEffect dynamicEffects;
	private @Getter @Setter MethodNode methodNode;

	private @Getter int access;
	
	public MethodRep(MethodInsnNode methodInsnNode, int access){
		super(Opcodes.ASM4);
		this.insnNode = methodInsnNode;
		this.access = access;
		this.isStatic = (access & Opcodes.ACC_STATIC) > 0;
		this.isNative = (access & Opcodes.ACC_NATIVE) > 0;
		this.isAbstract = (access & Opcodes.ACC_ABSTRACT) > 0;
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
	
	@Override
	public String toString(){
		return toString(new Types(false));
	}
	
	public String toString(Types table){
		return toString(insnNode,table);
	}
	
	public String toString(MethodInsnNode node, Types table){
		return String.format("%3d %3d %s",resolveTimeStamp,modifiedTimeStamp,
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
		if (isStatic) {
			return p.getArguments().size() + 1; // for this
		} else {
			return p.getArguments().size();
		}
	}
	
	public boolean isThis(int local){
		return local == 0 && isStatic;
	}
	
	public boolean isArg(int local){
		if(isThis(local)){
			return false;
		}
		if(local < argCount()){
			return true;
		}
		return false;
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		calls.add(new MethodInsnNode(opcode,owner,name,desc));
	}
	
	public boolean resolve(int newTimeStamp,final ClassFinder cf){
		try {
			ClassReader cr=new ClassReader(insnNode.owner);
			final MethodRep thisRep = this;
			
			final DepEffect analyzeResult = new DepEffect();
			
			log.info("Resolving {}",toString(new Types()));
			
			if(isAbstract){
				// do nothing
				log.info("Meet Abstract {}",toString(new Types()));
			}else if(isNative){
				analyzeResult.getOtherEffects().add(new NativeEffect());
			}else{
				cr.accept(new ClassVisitor(Opcodes.ASM4){
					@Override
					public MethodVisitor visitMethod(int access, String name,
							String desc, String signature, String[] exceptions) {
						if(!insnNode.name.equals(name)||!insnNode.desc.equals(desc)){
							return null;
						}
					
						return new MethodNode(Opcodes.ASM4,access,name,desc,signature,exceptions){
							@Override
							public void visitEnd() {
								super.visitEnd();
								methodNode = this;
								Analyzer<DepValue> ana = new Analyzer<DepValue>(new DepInterpreter(analyzeResult, thisRep,cf));
								try {
									/*Frame<DepValue> [] frames =*/ ana.analyze("dep", this);
								} catch (AnalyzerException e) {
									throw new RuntimeException("Error when analyzing",e);
								}
								
							}
						};
					}
				}, 0);
			}
			
			staticEffects = new DepEffect();
			staticEffects.merge(analyzeResult);
			for(MethodRep over:overrides){
				if(over.getDynamicEffects() != null){
					analyzeResult.merge(over.getDynamicEffects());
				}
			}
			
			if( dynamicEffects == null || !dynamicEffects.equals(analyzeResult) ){
				dynamicEffects = analyzeResult;
				this.modifiedTimeStamp = newTimeStamp;
				this.resolveTimeStamp = newTimeStamp;
				return true;
			}else{
				this.resolveTimeStamp = newTimeStamp;
				return false;
			}
		} catch (IOException e) {
			throw new RuntimeException("Class not found :"+insnNode.owner,e);
		}
	}
	
	public boolean needResolve(final ClassFinder cf){
		for(MethodRep rep:overrides){
			if(rep.getModifiedTimeStamp() == 0){
				return true;
			}
			if(resolveTimeStamp < rep.getModifiedTimeStamp()){
				return true;
			}
		}
		for(MethodInsnNode insn: calls){
			ClassRep crep = cf.loadClass(Types.binaryName2NormalName(insn.owner));
			
			MethodRep mrep ;
			if(insn.getOpcode() == Opcodes.INVOKEINTERFACE || insn.getOpcode() == Opcodes.INVOKEVIRTUAL){
				mrep=crep.getMethodVirtual(new MethodRep(insn,0).getId());
			}else{
				mrep=crep.getMethodStatic(new MethodRep(insn,0).getId());
			}
			
			if(mrep.getModifiedTimeStamp() == 0){
				return true;
			}
			if(resolveTimeStamp < mrep.getModifiedTimeStamp()){
				return true;
			}
		}
		return false;
	}
}
