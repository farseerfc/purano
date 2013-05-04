package jp.ac.osakau.farseerfc.purano.reflect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import jp.ac.osakau.farseerfc.purano.table.Types;
import lombok.Getter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ClassRep extends ClassVisitor {
	private final @Getter Map<String, MethodRep> methodMap = new HashMap<>();
	
	private final @Getter String name;
	
	private final ClassFinder classFinder;
	
	private final @Getter List<ClassRep> supers = new ArrayList<>();
//	private final @Getter Class<? extends Object> reflect;
	
	public ClassRep(String className, ClassFinder cf){
		super(Opcodes.ASM4);
		this.name = className;
		Class<? extends Object> cls = null;
//		try {
//			cls = Class.forName(className);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		this.reflect = cls;
		this.classFinder = cf;
		try {
			new ClassReader(className).accept(this, 0);
		} catch (IOException e) {
//			e.printStackTrace();
			throw new RuntimeException("Cannot load "+className ,e);
		}
		
	}
	
	public MethodRep getMethodStatic(String methodId){
		return methodMap.get(methodId);
	}
	
	public MethodRep getMethodVirtual(String methodId){
		MethodRep s = getMethodStatic(methodId);
		if(s == null){
			for(ClassRep sup: supers){
				return sup.getMethodVirtual(methodId);
			}
		}
		return s;
	}
	
	
	
	
//	public ClassRep(Class<? extends Object> reflect) throws IOException{
//		super(Opcodes.ASM4);
//		this.reflect = reflect;
//		if(reflect.isArray()){
//			this.name = ArrayStub.class.getName();
//		}else{
//			this.name = reflect.getName();
//		}
//		new ClassReader(this.name).accept(this, 0);
//	}
	
	public List<String> dump(Types table){
		List<String> result = new ArrayList<>();
		result.add(table.fullClassName(name));
		for(MethodRep m:methodMap.values()){
			result.addAll(m.dump(table));
		}
		return result;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
//		if(name.equals("<init>")){
//			return null;
//		}
		MethodRep rep = new MethodRep(new MethodInsnNode(0, this.name, name, desc));
		methodMap.put(rep.getId(),rep);
		for(ClassRep s : supers){
			s.override(rep.getId(),rep);
		}
		return rep;
	}
	
	public void override(String id, MethodRep overrider) {
		MethodRep overridded = getMethodStatic(id);
		if(overridded != null){
			overridded.getOverrides().add(overrider);
		}
		for(ClassRep s : supers){
			s.override(overrider.getId(),overrider);
		}
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		//super.visit(version, access, name, signature, superName, interfaces);
		
		if(!this.name.equals(Object.class.getName())){
			this.supers.add(classFinder.loadClass(Object.class.getName()));
		}
		
		this.supers.addAll(Lists.transform(Arrays.asList(interfaces), new Function<String,ClassRep>(){
			@Override @Nullable
			public ClassRep apply(@Nullable String name) {
				return classFinder.loadClass(name);
			}}));
	}
}
