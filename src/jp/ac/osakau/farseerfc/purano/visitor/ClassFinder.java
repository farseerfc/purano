package jp.ac.osakau.farseerfc.purano.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.google.common.base.Joiner;

public class ClassFinder {
	private final Map<String, Class<? extends Object>> classMap= new HashMap<>();
	private final String prefix;
	
	public ClassFinder(String prefix){
		this.prefix = prefix;
		findClasses();
	}
	
	private void findClasses(){
		Reflections reflections = new Reflections( prefix ,new SubTypesScanner(false));

        Set<Class<? extends Object>> allClasses = 
        	    reflections.getSubTypesOf(Object.class);
        for(Class<? extends Object> cls : allClasses){
        	classMap.put(cls.getName(), cls);
        }
	}
	
	public Set<String> dumpClass(){
		return classMap.keySet();
	}

	public static void main(String [] argv){
		String target="jp.ac.osakau.farseerfc.purano";
		ClassFinder cf = new ClassFinder(target);
        
        System.out.println(Joiner.on("\n").join(cf.dumpClass()));
	}
}
