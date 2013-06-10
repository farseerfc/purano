package jp.ac.osakau.farseerfc.purano.reflect;

import com.google.common.base.Joiner;
import jp.ac.osakau.farseerfc.purano.ano.Purity;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.net.MalformedURLException;
import java.util.*;

@Slf4j
public class ClassFinder {
	private @Getter final Map<String, ClassRep> classMap= new HashMap<>();

    private @NotNull @Getter List<MethodRep> toResolve = new ArrayList<>();
	private final Set<String> classTargets = new HashSet<>() ;
    private @Getter List<Integer> changedMethodsTrace = new ArrayList<>();
    private @Getter List<Integer> loadedClassesTrace = new ArrayList<>();

    private static final int MAX_LOAD_PASS=100;
	
	public ClassFinder(@NotNull Collection<String> prefix){
        findTargetClasses(prefix);

	}
	
	public ClassFinder(String string) {
		this(Arrays.asList(string));
	}

	public void resolveMethods() {
		int timestamp = 0;
		Set<ClassRep> allCreps = new HashSet<>(classMap.values());
		boolean changed;
		int pass = 0;
        int changedMethod = 0;

        changedMethodsTrace = new ArrayList<>();
        loadedClassesTrace = new ArrayList<>();
		do {
			changed = false;
			if(pass < MAX_LOAD_PASS){
			    allCreps = new HashSet<>(classMap.values());
                loadedClassesTrace.add(allCreps.size());
			}
            changedMethod = 0;
//            Set<MethodRep> changedSignatures = new HashSet<>();
			for (ClassRep crep : allCreps ) {
				for (MethodRep mrep : crep.getAllMethods()) {
					if (mrep.isNeedResolve(this)) {
						if (mrep.resolve(++timestamp, this)) {
							changed = true;
                            changedMethod ++;
//                            changedSignatures.add(mrep);
						}
					}
				}
			}
            changedMethodsTrace.add(changedMethod);
			System.out.println(String.format("Pass: %d Classes: %s Changed Method: %d [%s]",
                    pass++,allCreps.size(),changedMethod,
                    Joiner.on(", ").join(changedMethodsTrace)));
//            final int maxdump=4;
//            if(changedMethod>maxdump){
//                MethodRep [] top = new MethodRep [maxdump];
//                int i=0;
//                for(MethodRep mid : changedSignatures){
//                    if(i>=maxdump)break;
//                    top[i++]=mid;
//                }
//                System.out.println(Joiner.on(", ").join(top));
//            }else{
//                for(MethodRep m:changedSignatures){
//                    System.out.println(Joiner.on("\n").join(m.dump(this, new Types())));
//                }
//            }
		} while (changed);
	}
	
	private void findTargetClasses(@NotNull Collection<String> prefixes){
		
		for(String prefix:prefixes){
			Reflections reflections = new Reflections( prefix ,new SubTypesScanner(false));
	        classTargets.addAll( reflections.getStore().getSubTypesOf(Object.class.getName()));
		}
		for(String cls : classTargets){
			loadClass(cls);
		}
	}
	
	public ClassRep loadClass(@NotNull String classname){
		if(!classMap.containsKey(classname)){
			log.info("Loading {}", classname);
			if(classname.startsWith("[")){
				classMap.put(classname, new ClassRep(ArrayStub.class.getName(),this));
			}else{
				classMap.put(classname, new ClassRep(classname, this));
			}
		}
		return classMap.get(classname);
	}


    public void dump(@NotNull Types table){
        int method = 0, unknown = 0, stateless = 0, stateful = 0, modifier =0 ;
        int fieldM = 0, staticM = 0, argM = 0, nativeE = 0;

		for(String clsName : classMap.keySet()){
            if(classTargets.contains(clsName)){
                ClassRep cls = classMap.get(clsName);
                System.out.println(Joiner.on("\n").join(cls.dump(table)));
                for(MethodRep mtd: cls.getAllMethods()){
                    method++;
                    int p=mtd.purity();
                    if(p == Purity.Unknown){
                        unknown ++;
                    }
                    if(p == Purity.Stateless){
                        stateless ++;
                    }else if(p == Purity.Stateful){
                        stateful ++;
                    }else{
                        modifier ++;
                    }
                    if((p & Purity.ArgumentModifier)>0){
                        argM ++;
                    }
                    if((p & Purity.FieldModifier)>0){
                        fieldM ++;
                    }
                    if((p & Purity.StaticModifier)>0){
                        staticM ++;
                    }
                    if((p & Purity.Native)>0){
                        nativeE ++;
                    }
                }
            }
		}

        System.out.println(table.dumpImports());

        System.out.println("method "+method);
        System.out.println("unknown "+unknown);
        System.out.println("stateless "+stateless);
        System.out.println("stateful "+stateful);
        System.out.println("modifier "+modifier);
        System.out.println("fieldM "+fieldM);
        System.out.println("staticM "+staticM);
        System.out.println("argM "+argM);
        System.out.println("nativeE "+nativeE);
	}

	public static void main(@NotNull String [] argv) throws MalformedURLException {
		long start=System.currentTimeMillis();
//		String targetPackage []={"org.htmlparser"};
        String targetPackage []={"org.argouml"};
//        String targetPackage []={"org.apache.catalina"};
//        String targetPackage []={"jp.ac.osakau.farseerfc.purano","org.objectweb.asm"};
//        String targetPackage []={"jp.ac.osakau.farseerfc.purano.test"};
		if(argv.length > 1){
			targetPackage=argv;
		}
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage));
		cf.resolveMethods();


        Types table = new Types(true, targetPackage[0]);
//        Types table = new Types(false);
        cf.dump(table);

        System.out.println("Loaded Classes: "+Joiner.on(", ").join(cf.getLoadedClassesTrace()));
        System.out.println("Changed methods: "+Joiner.on(", ").join(cf.getChangedMethodsTrace()));
        System.out.println("Runtime :"+(System.currentTimeMillis() - start));
	}
}
