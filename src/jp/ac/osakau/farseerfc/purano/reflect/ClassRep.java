package jp.ac.osakau.farseerfc.purano.reflect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import lombok.Getter;

public class ClassRep extends ClassVisitor {
	private final @Getter Map<String, MethodRep> methodMap = new HashMap<>();
	
	private final @Getter String name;
	private final @Getter Class<? extends Object> reflect;
	
	public ClassRep(String className){
		super(Opcodes.ASM4);
		this.name = className;
		Class<? extends Object> cls = null;
		try {
			cls = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		this.reflect = cls;
		
		try {
			new ClassReader(cls.getName()).accept(this, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public ClassRep(Class<? extends Object> reflect){
		super(Opcodes.ASM4);
		this.reflect = reflect;
		this.name = reflect.getName();
		
		try {
			new ClassReader(reflect.getName()).accept(this, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

//	public void findMethods(){
//		for(Method m : reflect.getMethods()){
//			MethodRep rep =  new MethodRep(m, name);
//			methodMap.put(rep.getId(),rep);
//		}
//	}
	
	public List<String> dump(){
		List<String> result = new ArrayList<>();
		result.add(name);
		for(MethodRep m:methodMap.values()){
			result.addAll(m.dump());
		}
		return result;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if(name.equals("<init>")){
			return null;
		}
		MethodRep rep = new MethodRep(new MethodInsnNode(0, this.name, name, desc));
		methodMap.put(rep.getId(),rep);
		return rep;
	}
}
