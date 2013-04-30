package jp.ac.osakau.farseerfc.purano.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import jp.ac.osakau.farseerfc.purano.table.Types;

import org.objectweb.asm.tree.MethodInsnNode;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.libs.org.objectweb.asm.Type;

public class MethodRep {
	private final @Getter MethodInsnNode node;
	private final @Getter Method reflect;
	private final @Getter List<MethodRep> overrides = new ArrayList<>();
	private final @Getter List<MethodInsnNode> calls = new ArrayList<>();
	
	public MethodRep(MethodInsnNode methodNode){
		this.node = methodNode;
		this.reflect = getReflectFromNode(methodNode);
	}
	
	public MethodRep(Method reflect){
		this.reflect = reflect;
		this.node = new MethodInsnNode(0, 
				Types.normalName2BinaryName(reflect.getDeclaringClass().getName()), 
				reflect.getName(), 
				Type.getMethodDescriptor(reflect));
	}

	private Method getReflectFromNode(MethodInsnNode node) {
		try {
			Class<? extends Object> cls = Class.forName(Types.binaryName2NormalName(node.owner));
			return cls.getMethod(
					node.name,
					Lists.transform(
							Lists.newArrayList(Type.getType(node.desc)
									.getArgumentTypes()),
							new Function<Type, Class<? extends Object>>() {
								@Override @Nullable
								public Class<? extends Object> apply( @Nullable Type t) {
									try {
										return Class.forName(t.getClassName());
									} catch (ClassNotFoundException e) {
										e.printStackTrace();
									}
									return null;
								}
							}).toArray(new Class[0]));
		} catch (NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(reflect, node.desc, node.name,node.owner);
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof MethodRep){
			return this.equals(other);
		}
		return false;
	}
	
	public boolean equals(MethodRep other){
		if (!Objects.equal(this.reflect, other.reflect))
			return false;
		if (!Objects.equal(this.node.desc, other.node.desc))
			return false;
		if (!Objects.equal(this.node.name, other.node.name))
			return false;
		if (!Objects.equal(this.node.owner, other.node.owner))
			return false;
		return true;
	}
}
