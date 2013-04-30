package jp.ac.osakau.farseerfc.purano.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.google.common.base.Joiner;

public class ClassFinder {
	private final Map<String, ClassRep> classMap;
	private final String prefix;
	private final Map<Method, Class<? extends Object>> methodMap = new HashMap<>();
	
	public ClassFinder(String prefix){
		this.prefix = prefix;
		this.classMap = findClasses(prefix);
		for(ClassRep cls : classMap.values()){
			findMethods(cls);
		}
	}
	
	private Map<String, ClassRep> findClasses(String prefix){
		final Map<String, ClassRep> classMap= new HashMap<>();
		Reflections reflections = new Reflections( prefix ,new SubTypesScanner(false));

        Set<Class<? extends Object>> allClasses = 
        	    reflections.getSubTypesOf(Object.class);
        for(Class<? extends Object> cls : allClasses){
        	classMap.put(cls.getName(), new ClassRep(cls));
        }
        return classMap;
	}
	
	private void findMethods(ClassRep cls){
		for(Method m : cls.getReflect().getMethods()){
			if(m.getDeclaringClass().equals(cls)){
				methodMap.put(m, null);
			}else{
				methodMap.put(m, m.getDeclaringClass());
			}
		}
	}
	
	public List<String> dump(){
		List<String> result = new ArrayList<>();
		for(String className : classMap.keySet()){
			result.add(className);
		}
		for(Method m : methodMap.keySet()){
			result.add(String.format("%s -> %s",m.getName(),methodMap.get(m)));
		}
		return result;
	}

	public static void main(String [] argv){
		String target="jp.ac.osakau.farseerfc.purano";
		ClassFinder cf = new ClassFinder(target);
        
        System.out.println(Joiner.on("\n").join(cf.dump()));
	}
}
