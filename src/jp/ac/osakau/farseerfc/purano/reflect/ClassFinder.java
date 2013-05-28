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
    @NotNull
    private @Getter List<MethodRep> toResolve = new ArrayList<>();
	private final Set<String> classTargets = new HashSet<>() ;
	
	public ClassFinder(@NotNull Collection<String> prefix){
        findClasses(prefix);
		
//		Set<String> toLoadClass;
//		Set<String> loadedClass = new HashSet<>();
//		int pass=0;
//		do{
//			toLoadClass = new HashSet<>(Sets.difference(classMap.keySet(),loadedClass));
//			loadedClass = new HashSet<>(classMap.keySet());
//			for(String clsName : toLoadClass){
//				findMethods(classMap.get(clsName));
//			}
//			pass++;
//			System.out.println("Loaded Class :"+loadedClass.size());
//			System.out.println("Passes :"+pass);
//		}while(loadedClass.size() < classMap.size());

//		resolveMethods();

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
			if(pass < 3){
				allCreps = new HashSet<>(classMap.values());
			}
			for (ClassRep crep : allCreps ) {
				for (MethodRep mrep : crep.getAllMethods()) {
					if (mrep.needResolve(this)) {
						if (mrep.resolve(++timestamp, this)) {
							changed = true;
						}
					}
				}
			}
			System.out.println("Pass: " + ++pass + " classes: "+ allCreps.size());
		} while (changed);
	}
	
//	public boolean resolve(Set<ClassRep> allCreps, int newTimeStamp){
//		for (ClassRep crep : allCreps) {
//			for (MethodRep mrep : crep.getAllMethods()) {
//				if (mrep.needResolve(this)) {
//					if( mrep.resolve(newTimeStamp, this)){
//						return true;
//					}
//				}
//			}
//		}
//		return false;
//	}
	
	private void findClasses(@NotNull Collection<String> prefixes){
		
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
//	public void loadClass(Class<? extends Object> cls) throws IOException{
//		if(classMap.containsKey(cls.getName())){
//			return ;
//		}
//		//System.out.println("Loading "+cls.getName());
//		classMap.put(cls.getName(), new ClassRep(cls));
//	}
//	
//	private void findMethods(ClassRep cls){
//		for (MethodRep m : cls.getMethodMap().values()) {
//			if(m.getReflect() == null) continue;
//			
//			Class<? extends Object> decl = m.getReflect().getDeclaringClass();
//			if (!decl.equals(cls.getReflect())) {
//				System.err.printf("Decl %s, cls %s\n",decl.getName(),cls.getName());
//				MethodRep rep;
//				try {
//					rep = new MethodRep(
//							cls.getReflect().getDeclaredMethod(
//								m.getReflect().getName(),
//								m.getReflect().getParameterTypes()),
//							cls.getName());
//
//					getClass(decl.getName())
//						.getMethodMap()
//						.get(rep.getId())
//						.getOverrides().add(m);
//				} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IOException |UnsatisfiedLinkError e) {
//					throw new RuntimeException(e);
//				}
//			}
//			for(MethodInsnNode call : m.getCalls()){
//				String className = Types.binaryName2NormalName(call.owner);
//				try {
//					Class<?> clazz=Types.forName(className); 
//					loadClass(clazz);
//				} catch (ClassNotFoundException | NoClassDefFoundError | IOException |UnsatisfiedLinkError e) {
//					System.err.printf("Warning: Cannot load method of class \"%s\" %s\n",className,e);
//					System.err.flush();
//				}
//			}
//		}
//	}
	
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
