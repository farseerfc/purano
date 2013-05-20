package jp.ac.osakau.farseerfc.purano.reflect;

import com.google.common.base.Objects;
import jp.ac.osakau.farseerfc.purano.dep.DepEffect;
import jp.ac.osakau.farseerfc.purano.dep.DepInterpreter;
import jp.ac.osakau.farseerfc.purano.dep.DepValue;
import jp.ac.osakau.farseerfc.purano.effect.NativeEffect;
import jp.ac.osakau.farseerfc.purano.util.Escape;
import jp.ac.osakau.farseerfc.purano.util.MethodDesc;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.IOException;
import java.util.*;

@Slf4j
public class MethodRep extends MethodVisitor {
	
	@NotNull
    private final @Getter MethodInsnNode insnNode;
	private final @Getter Map<String, MethodRep> overrides = new HashMap<>();
	private final @Getter Set<MethodInsnNode> calls = new HashSet<>();
	
	@Nullable
    private final @Getter MethodDesc desc ;
	private final @Getter boolean isStatic ;
	private final @Getter boolean isNative;
	private final @Getter boolean isAbstract;
	private final @Getter boolean isInit;
	
	private @Getter int modifiedTimeStamp;
	private @Getter int resolveTimeStamp;
	private @Getter DepEffect staticEffects;
	private @Getter DepEffect dynamicEffects;
	private @Getter @Setter MethodNode methodNode;

	private @Getter int access;

	
	public MethodRep(@NotNull MethodInsnNode methodInsnNode, int access){
		super(Opcodes.ASM4);
		this.insnNode = methodInsnNode;
		this.access = access;
		this.isStatic = (access & Opcodes.ACC_STATIC) > 0;
		this.isNative = (access & Opcodes.ACC_NATIVE) > 0;
		this.isAbstract = (access & Opcodes.ACC_ABSTRACT) > 0;
		this.isInit = methodInsnNode.name.equals("<init>");
		desc=new Types(false).method2full(methodInsnNode.desc);
	}

	@NotNull
    public String getId(){
		return getId(insnNode);
	}
	
	@NotNull
    public static String getId(@NotNull MethodInsnNode insnNode){
		return insnNode.name+insnNode.desc;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(insnNode.desc, insnNode.name,insnNode.owner);
	}
	
	@Override
	public boolean equals(@Nullable Object other){
        return other != null && other instanceof MethodRep && this.equals(other);
    }
	
	@Override
	public String toString(){
		return toString(new Types(false));
	}
	
	public String toString(@NotNull Types table){
		return String.format("%3d %3d %s",resolveTimeStamp,modifiedTimeStamp,
		table.dumpMethodDesc(insnNode.desc, 
				String.format("%s#%s", 
						table.fullClassName(insnNode.owner),
						insnNode.name)));
	}
	
	public boolean equals(@NotNull MethodRep other){
        return Objects.equal(this.insnNode.desc, other.insnNode.desc) &&
                Objects.equal(this.insnNode.name, other.insnNode.name) &&
                Objects.equal(this.insnNode.owner, other.insnNode.owner);
    }
	
	@NotNull
    public List<String> dump(@NotNull ClassFinder classFinder, @NotNull Types table){
		List<String> result = new ArrayList<>();
		if(dynamicEffects != null && getMethodNode() != null){
			
			result.add("    "+Escape.methodName(toString(table)));
			for(MethodRep rep : overrides.values()){
				result.add(String.format("        # %s", rep.toString(table)));
			}
			for(MethodInsnNode insn : calls){
				//log.info("Load when dump {}",Types.binaryName2NormalName(insn.owner));
				if(classFinder.getClassMap().containsKey(Types.binaryName2NormalName(insn.owner))){
					MethodRep mr = classFinder.loadClass(Types.binaryName2NormalName(insn.owner)).getMethodVirtual(MethodRep.getId(insn));
					if(mr != null){
						result.add(String.format("        > %s", mr.toString(table)));
					}
				}else{
					result.add(String.format("        >   /   / %s",
							table.dumpMethodDesc(insn.desc, 
									String.format("%s#%s", 
											table.fullClassName(insn.owner),
											insn.name))));
				}
			}
		
//		if(dynamicEffects != null && getMethodNode() != null){
			result.add(dynamicEffects.dump(this, table,"            "));
		}
		return result;
	}
	
	
	public int argCount(){
		MethodDesc p = new Types().method2full(insnNode.desc);
		if (!isStatic) {
			return p.getArguments().size() + 1; // for this
		} else {
			return p.getArguments().size();
		}
	}
	
	public boolean isThis(int local){
		return local == 0 && !isStatic;
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
			final DepEffect analyzeResult = new DepEffect();
			
//			log.info("Resolving {}",toString(new Types()));
			
			if(isAbstract){
				// do nothing
//				log.info("Meet Abstract {}",toString(new Types()));
			}else if(isNative){
				analyzeResult.getOtherEffects().add(new NativeEffect(null));
			}else{
				if(methodNode == null){
					final MethodRep thisRep = this;
					ClassReader cr=new ClassReader(insnNode.owner);
					cr.accept(new ClassVisitor(Opcodes.ASM4){
						@Nullable
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
				}else{
					Analyzer<DepValue> ana = new Analyzer<>(new DepInterpreter(analyzeResult, this,cf));
					try {
						/*Frame<DepValue> [] frames =*/ ana.analyze("dep", methodNode);
					} catch (AnalyzerException e) {
						throw new RuntimeException("Error when analyzing",e);
					}
				}
			}
			
			staticEffects = new DepEffect();
			staticEffects.merge(analyzeResult, null);
			for(MethodRep over:overrides.values()){
				if(over.getDynamicEffects() != null){
					analyzeResult.merge(over.getDynamicEffects(),over);
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
	
	public boolean needResolve(@NotNull final ClassFinder cf){
		if(modifiedTimeStamp == 0){
			return true;
		}
		for(MethodRep rep:overrides.values()){
			if(rep.getModifiedTimeStamp() == 0){
				return true;
			}
			if(resolveTimeStamp <= rep.getModifiedTimeStamp()){
				return true;
			}
		}
		for(MethodInsnNode insn: calls){
			ClassRep crep = cf.loadClass(Types.binaryName2NormalName(insn.owner));
			
			MethodRep mrep = crep.getMethodVirtual(new MethodRep(insn,0).getId());
			
			if(mrep == null){
				log.error("Cannot find method {} in class {} opcode {} ",new MethodRep(insn,0).getId(),crep.getName(), insn.getOpcode());
				Types.notFound("Cannot find method ", null);
				return false;
			}
			
			if(mrep.getModifiedTimeStamp() == 0){
				return true;
			}
			if(resolveTimeStamp <= mrep.getResolveTimeStamp()){
				return true;
			}
		}
		return false;
	}
}
