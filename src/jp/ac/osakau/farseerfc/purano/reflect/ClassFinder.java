package jp.ac.osakau.farseerfc.purano.reflect;

import com.google.common.base.Joiner;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.*;

@Slf4j
public class ClassFinder {
	private @Getter final Map<String, ClassRep> classMap= new HashMap<>();

    private @NotNull @Getter List<MethodRep> toResolve = new ArrayList<>();
	private final Set<String> classTargets = new HashSet<>() ;
	
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
		do {
			changed = false;
			if(pass < 4){
				allCreps = new HashSet<>(classMap.values());
			}
			for (ClassRep crep : allCreps ) {
				for (MethodRep mrep : crep.getAllMethods()) {
					if (mrep.isNeedResolve(this)) {
						if (mrep.resolve(++timestamp, this)) {
							changed = true;
						}
					}
				}
			}
			System.out.println("Pass: " + ++pass + " classes: "+ allCreps.size());
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

	
	@NotNull
    public List<String> dump(@NotNull Types table){
		List<String> result = new ArrayList<>();
		for(String clsName : classMap.keySet()){
			if(classTargets.contains(clsName)){
				ClassRep cls = classMap.get(clsName);
				result.addAll(cls.dump(table));
			}
			//result.add(cls.getName());
		}
		return result;
	}

	public static void main(@NotNull String [] argv){
		long start=System.currentTimeMillis();
//		String targetPackage []={"sun.util.calendar"};
        String targetPackage []={"jp.ac.osakau.farseerfc.purano.test"};
		if(argv.length > 1){
			targetPackage=argv;
		}
//		String targetPackage="java.lang";
		ClassFinder cf = new ClassFinder(Arrays.asList(targetPackage));
		cf.resolveMethods();
//        cf.loadClass("jp.ac.osakau.farseerfc.purano.test.TargetA").getMethodStatic("staticAdd(II)I").resolve(0, cf);
//        cf.loadClass("jp.ac.osakau.farseerfc.purano.test.TargetA").getMethodStatic("setC(I)V").resolve(1, cf);
//        cf.loadClass("jp.ac.osakau.farseerfc.purano.test.TargetA").getMethodStatic("func(I)V").resolve(2, cf);
//        cf.loadClass("jp.ac.osakau.farseerfc.purano.test.TargetA").getMethodStatic("setC(I)V").resolve(3, cf);
//        cf.loadClass("jp.ac.osakau.farseerfc.purano.test.TargetA").getMethodStatic("func(I)V").resolve(4, cf);
        
        //rep.resolve(1);
        //System.out.println(rep.getEffects().dump(rep.getMethodNode(), new TypeNameTable()));
//		cf.loadClass("java.lang.AbstractStringBuilder");
//		ClassRep targetA = cf.loadClass("java.lang.StringBuilder");
//		MethodRep mr = targetA.getMethodVirtual("getChars(II[CI)V");
//		System.out.println(targetA.getSupers().get(1).getName());

        Types table = new Types(true, targetPackage[0]);
        
        System.out.println(Joiner.on("\n").join(cf.dump(table)));
        System.out.println(table.dumpImports());
        
        System.out.println("Runtime :"+(System.currentTimeMillis() - start));
	}
}
