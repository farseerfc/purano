package jp.ac.osakau.farseerfc.purano.reflect;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class ClassRep {
	private final @Getter List<MethodRep> methods = new ArrayList<>();
	
	private final @Getter String name;
	private final @Getter Class<? extends Object> reflect;
	
	public ClassRep(String className){
		this.name = className;
		Class<? extends Object> cls = null;
		try {
			cls = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		this.reflect = cls;
	}
	
	public ClassRep(Class<? extends Object> reflect){
		this.reflect = reflect;
		this.name = reflect.getName();
	}
}
