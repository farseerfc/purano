package jp.ac.osakau.farseerfc.purano.reflect;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

import jp.ac.osakau.farseerfc.purano.ano.Purity;
import jp.ac.osakau.farseerfc.purano.ano.StaticField;
import jp.ac.osakau.farseerfc.purano.dep.DepAnalyzer;
import jp.ac.osakau.farseerfc.purano.dep.DepEffect;
import jp.ac.osakau.farseerfc.purano.dep.DepInterpreter;
import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.effect.NativeEffect;
import jp.ac.osakau.farseerfc.purano.util.Escaper;
import jp.ac.osakau.farseerfc.purano.util.MethodDesc;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.IOException;
import java.util.*;

@Slf4j
public class MethodRep extends MethodVisitor implements Purity {
	
	@NotNull
    private final @Getter MethodInsnNode insnNode;
	private final @Getter Map<String, MethodRep> overrided = new HashMap<>();
    private final @Getter Set<MethodRep> overrides = new HashSet<>();
	private final @Getter Set<MethodInsnNode> calls = new HashSet<>();
	private final @Getter Set<MethodRep> called = new HashSet<>();
	
	@Nullable
    private final @Getter MethodDesc desc ;
	private final @Getter boolean isStatic ;
	private final @Getter boolean isNative;
	private final @Getter boolean isAbstract;
	private final @Getter boolean isInit;
    private final int [] argPos;
	
	private @Getter int modifiedTimeStamp;
	private @Getter int resolveTimeStamp;
	private final @Getter DepEffect staticEffects = new DepEffect();
	private final @Getter DepEffect dynamicEffects = new DepEffect();
	private @Getter @Setter MethodNode methodNode;

	private @Getter int access;

    private @Getter @Setter boolean needResolve;
    
    private @Getter final DepSet cacheSemantic;

	
	public MethodRep(@NotNull MethodInsnNode methodInsnNode, int access){
		super(Opcodes.ASM5);
		this.insnNode = methodInsnNode;
		this.access = access;
		this.isStatic = (access & Opcodes.ACC_STATIC) > 0;
		this.isNative = (access & Opcodes.ACC_NATIVE) > 0;
		this.isAbstract = (access & Opcodes.ACC_ABSTRACT) > 0;
		this.isInit = methodInsnNode.name.equals("<init>");
		desc=new Types(false).method2full(methodInsnNode.desc);

        List<Type> argTypes = new ArrayList<>(Arrays.asList(Type.getMethodType(this.insnNode.desc).getArgumentTypes()));
        List<Integer> argPosMap = new ArrayList<>();
        if(!isStatic){
            argTypes.add(Type.getObjectType(insnNode.owner));
        }
        for(int i=0;i<argTypes.size();++i){
            for(int j=0;j<argTypes.get(i).getSize();++j)    {
                argPosMap.add(i);
            }
        }
        argPos = ArrayUtils.toPrimitive(argPosMap.toArray(new Integer [argPosMap.size()]));

        needResolve = true;
        
        cacheSemantic = new DepSet();
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
        if( other == null )
            return false;
        if(!( other instanceof MethodRep ))
            return false;

        MethodRep obj = (MethodRep) other;
        if(!obj.insnNode.desc.equals(this.insnNode.desc))
            return false;
        if(!obj.insnNode.name.equals(this.insnNode.name))
            return false;
        if(!obj.insnNode.owner.equals(this.insnNode.owner))
            return false;
        return true;
    }

    public int localToArgumentPos(int local){
        return argPos[local];
    }
	
	@Override
	public String toString(){
		return toString(new Types(false));
	}
	
	public String toString(@NotNull Types table){
		return //String.format("%3d %3d %s",resolveTimeStamp,modifiedTimeStamp,
		table.dumpMethodDesc(insnNode.desc, 
				String.format("%s#%s", 
						table.fullClassName(insnNode.owner),
						insnNode.name));//);
	}
	
	public boolean equals(@NotNull MethodRep other){
        return Objects.equal(this.insnNode.desc, other.insnNode.desc) &&
                Objects.equal(this.insnNode.name, other.insnNode.name) &&
                Objects.equal(this.insnNode.owner, other.insnNode.owner);
    }
	
	@NotNull
    public List<String> dump(@NotNull ClassFinder classFinder, @NotNull Types table, Escaper esc){
		List<String> result = new ArrayList<>();
		if(getMethodNode() != null){
			
			result.add("    "+esc.methodName(toString(table)));
			for(MethodRep rep : overrided.values()){
				result.add(String.format("        # %s", rep.toString(table)));
			}

            if(dynamicEffects != null ){
                for(MethodInsnNode insn : calls){
                    //log.info("Load when dump {}",Types.binaryName2NormalName(insn.owner));
                    if(classFinder.getClassMap().containsKey(Types.binaryName2NormalName(insn.owner))){
                        MethodRep mr = classFinder.loadClass(Types.binaryName2NormalName(insn.owner)).
                                getMethodVirtual(MethodRep.getId(insn));
                        if(mr != null){
                            result.add(String.format("        = %s", mr.toString(table)));
                        }
                    }else{
                        result.add(String.format("        ? %s",
                                table.dumpMethodDesc(insn.desc,
                                        String.format("%s#%s",
                                                table.fullClassName(insn.owner),
                                                insn.name))));
                    }
                }

                result.add("            "+esc.purity(dumpPurity()));
                result.add(dynamicEffects.dump(this, table,"            ", esc));
            }

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
        return !isThis(local) && local < argPos.length;
    }
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		calls.add(new MethodInsnNode(opcode,owner,name,desc, opcode == Opcodes.INVOKEINTERFACE));
	}
	
	public boolean resolve(int newTimeStamp, @NotNull final ClassFinder cf){
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
					cr.accept(new ClassVisitor(Opcodes.ASM5){
						@Nullable
                        @Override
						public MethodVisitor visitMethod(int access, String name,
								String desc, String signature, String[] exceptions) {
							if(!insnNode.name.equals(name)||!insnNode.desc.equals(desc)){
								return null;
							}
						
							return new MethodNode(Opcodes.ASM5,access,name,desc,signature,exceptions){
								@Override
								public void visitEnd() {
									super.visitEnd();
									methodNode = this;
									DepAnalyzer ana = new DepAnalyzer(new DepInterpreter(analyzeResult, thisRep, cf));
									try {
										/*Frame<DepValue> [] frames =*/ ana.analyze("dep", this);
									} catch (AnalyzerException e) {
										//throw new RuntimeException("Error when analyzing",e);
                                        log.warn("Error when analyzing {}",e);
									}
									
								}
							};
						}
					}, 0);
				}else{
                    DepAnalyzer ana = new DepAnalyzer(new DepInterpreter(analyzeResult, this,cf));
					try {
						/*Frame<DepValue> [] frames =*/ ana.analyze("dep", methodNode);
					} catch (AnalyzerException e) {
//						throw new RuntimeException("Error when analyzing",e);
                        log.warn("Error when analyzing {}",e);
					}
				}
			}
			
			staticEffects.merge(analyzeResult, null);

			for(MethodRep over:overrided.values()){
				if(over.getDynamicEffects() != null){
					analyzeResult.merge(over.getDynamicEffects(),over);
				}
			}

            needResolve = false;

			if(!dynamicEffects.equals(analyzeResult) ){
				dynamicEffects.merge(analyzeResult, null);
				
				this.modifiedTimeStamp = newTimeStamp;
				this.resolveTimeStamp = newTimeStamp;

                for(MethodRep methodRep:called){
                    methodRep.setNeedResolve(true);
                }
                for(MethodRep methodRep:overrides){
                    methodRep.setNeedResolve(true);
                }

				return true;
			}else{
				this.resolveTimeStamp = newTimeStamp;
				return false;
			}


		} catch (IOException e) {
			throw new RuntimeException("Class not found :"+insnNode.owner,e);
		}
	}

    public boolean isNeedResolve(@NotNull final ClassFinder cf){
        return needResolve;
    }

	public boolean isNeedResolveOld(@NotNull final ClassFinder cf){
		if(modifiedTimeStamp == 0){
			return true;
		}
		for(MethodRep rep:overrided.values()){
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

    public void override(@NotNull MethodRep overrider) {
        if (!getId().equals(overrider.getId())) {
            throw new AssertionError("Overrider and overridee should have same Id :" + getId());
        }
        overrided.put(overrider.getInsnNode().owner, overrider);
        overrider.getOverrides().add(this);
    }

    public int purity(){
        if(dynamicEffects == null){
            return Unknown;
        }
        int result = 0;
        if(dynamicEffects.getOtherEffects().contains(new NativeEffect(null))){
            result |= Native;
        }
        if(dynamicEffects.getStaticField().size()>0){
            result |= StaticModifier;
        }
        if(dynamicEffects.getThisField().size()>0){
            result |= FieldModifier;
        }
        if(dynamicEffects.getArgumentEffects().size()>0){
            result |= ArgumentModifier;
        }
        if(dynamicEffects.getReturnDep().getDeps().getFields().size() >0){
            result |= Stateful;
        }else{
            result |= Stateless;
        }
        return result;
    }

    public String dumpPurity(){
        List<String> result=new ArrayList<>();
        int p = purity();
        if(p == Unknown){
            result.add("Unknown");
        }

        if(p == Stateless){
            result.add("Stateless");
        }

        if((p & Stateful) > 0){
            result.add("Stateful");
        }
        if((p & ArgumentModifier) > 0){
            result.add("ArgumentModifier");
        }
        if((p & FieldModifier) > 0){
            result.add("FieldModifier");
        }
        if((p & StaticModifier) > 0){
            result.add("StaticModifier");
        }
        if((p & Native) > 0){
            result.add("Native");
        }
        return Joiner.on(", ").join(result);
    }
}
