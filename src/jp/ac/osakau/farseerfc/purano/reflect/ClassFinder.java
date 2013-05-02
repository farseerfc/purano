package jp.ac.osakau.farseerfc.purano.reflect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.ac.osakau.farseerfc.purano.table.TypeNameTable;
import lombok.Getter;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class ClassFinder {
	private @Getter Map<String, ClassRep> classMap;
	private final String prefix;
	
	
	public ClassFinder(String prefix){
		this.prefix = prefix;
		this.classMap = findClasses(prefix);
		List<ClassRep> allClass;
		
		int pass=0;
		do{
			allClass = Lists.newArrayList(classMap.values());
			for(ClassRep cls : allClass){
				findMethods(cls);
			}
			pass++;
		}while(allClass.size() < classMap.size());
		System.out.println("Passes :"+pass);
		
	}
	
	private Map<String, ClassRep> findClasses(String prefix){
		final Map<String, ClassRep> classMap= new HashMap<>();
		Reflections reflections = new Reflections( prefix ,new SubTypesScanner(false));

        Set<Class<? extends Object>> allClasses = 
        	    reflections.getSubTypesOf(Object.class);
        for(Class<? extends Object> cls : allClasses){
        	loadClass(classMap,cls);
        }
        return classMap;
	}
	
	public void loadClass(Map<String, ClassRep> classMap,Class<? extends Object> cls){
		classMap.put(cls.getName(), new ClassRep(cls));
	}
	
	private void findMethods(ClassRep cls){
		for (MethodRep m : cls.getMethodMap().values()) {
			Class<? extends Object> decl = m.getReflect().getDeclaringClass();
			if (!decl.equals(cls.getReflect())) {
				System.err.printf("Decl %s, cls %s\n",decl.getName(),cls.getName());
				MethodRep rep;
				try {
					rep = new MethodRep(
							cls.getReflect().getDeclaredMethod(
								m.getReflect().getName(),
								m.getReflect().getParameterTypes()),
							cls.getName());

					getClass(decl.getName())
						.getMethodMap()
						.get(rep.getId())
						.getOverrides().add(m);
				} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	public List<String> dump(TypeNameTable table){
		List<String> result = new ArrayList<>();
		for(ClassRep cls : classMap.values()){
			result.addAll(cls.dump(table));
		}
		return result;
	}
	
	public ClassRep getClass(String className) throws ClassNotFoundException{
		if(!classMap.containsKey(className)){
			loadClass(classMap, Class.forName(className));
		}
		return classMap.get(className);
	}

	public static void main(String [] argv){
		long start=System.currentTimeMillis();
		String target="jp.ac.osakau.farseerfc.purano";
		ClassFinder cf = new ClassFinder(target);
        //MethodRep rep=cf.getClassMap().get("jp.ac.osakau.farseerfc.purano.test.TargetA").getMethodMap().get("staticAdd(II)I");
        //rep.resolve(1);
        //System.out.println(rep.getEffects().dump(rep.getMethodNode(), new TypeNameTable()));
        TypeNameTable table = new TypeNameTable();
        System.out.println(Joiner.on("\n").join(cf.dump(table)));
        System.out.println(table.dumpImports());
        
        System.out.println("Runtime :"+(System.currentTimeMillis() - start));
	}
}
