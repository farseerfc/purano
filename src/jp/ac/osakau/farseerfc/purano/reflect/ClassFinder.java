package jp.ac.osakau.farseerfc.purano.reflect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.Getter;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class ClassFinder {
	private static final Logger log= LoggerFactory.getLogger(ClassFinder.class);
	
	private @Getter Map<String, ClassRep> classMap= new HashMap<>();
	private final String prefix;
	private @Getter List<MethodRep> toResolve = new ArrayList<>(); 
	
	public ClassFinder(String prefix){
		this.prefix = prefix;
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
//		
	}
	
	private void findClasses(String prefix){
		Reflections reflections = new Reflections( prefix ,new SubTypesScanner(false));
        Set<String> allClasses = reflections.getStore().getSubTypesOf(Object.class.getName());
        for(String cls : allClasses){
			loadClass(cls);
        }
	}
	
	public ClassRep loadClass(String classname){
		if(!classMap.containsKey(classname)){
			log.info("Loading {}", classname);
			classMap.put(classname, new ClassRep(classname, this));
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
	
	public List<String> dump(Types table){
		List<String> result = new ArrayList<>();
		for(ClassRep cls : classMap.values()){
			result.addAll(cls.dump(table));
			//result.add(cls.getName());
		}
		return result;
	}

	public static void main(String [] argv){
		long start=System.currentTimeMillis();
		String targetPackage="jp.ac.osakau.farseerfc.purano.test";
		ClassFinder cf = new ClassFinder(targetPackage);
        //MethodRep rep=cf.getClassMap().get("jp.ac.osakau.farseerfc.purano.test.TargetA").getMethodMap().get("staticAdd(II)I");
        //rep.resolve(1);
        //System.out.println(rep.getEffects().dump(rep.getMethodNode(), new TypeNameTable()));
        Types table = new Types(true);
        System.out.println(Joiner.on("\n").join(cf.dump(table)));
        System.out.println(table.dumpImports());
        
        System.out.println("Runtime :"+(System.currentTimeMillis() - start));
	}
}
