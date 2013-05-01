package jp.ac.osakau.farseerfc.purano.reflect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class ClassFinder {
	private final Map<String, ClassRep> classMap;
	private final String prefix;
	
	public ClassFinder(String prefix){
		this.prefix = prefix;
		this.classMap = findClasses(prefix);
		List<ClassRep> allClass = Lists.newArrayList(classMap.values());
		for(ClassRep cls : allClass){
				findMethods(cls);
		}
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
			if (!decl.equals(cls)) {
				MethodRep rep;
				try {
					rep = new MethodRep(
							decl.getDeclaredMethod(
								m.getReflect().getName(),
								m.getReflect().getParameterTypes()),
							decl.getName());

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
	
	public List<String> dump(){
		List<String> result = new ArrayList<>();
		for(ClassRep cls : classMap.values()){
			result.addAll(cls.dump());
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
		String target="jp.ac.osakau.farseerfc.purano";
		ClassFinder cf = new ClassFinder(target);
        
        System.out.println(Joiner.on("\n").join(cf.dump()));
	}
}
