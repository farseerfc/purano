package jp.ac.osakau.farseerfc.asm.dep;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

import org.objectweb.asm.tree.MethodNode;

import com.google.common.base.Joiner;

public @Data class DepSet {
	private final Set<String> statics = new HashSet<>();
	// this pointer, Args, and true locals, all in the locals list, by the order
	private final Set<Integer> locals = new HashSet<>();
	
	private final Set<String> fields = new HashSet<>();
	
	public DepSet(){}
	
	public DepSet(DepSet ... others){
		for(DepSet o : others){
			this.merge(o);
		}
	}

	public void merge(DepSet other){
		this.statics.addAll(other.statics);
		this.locals.addAll(other.locals);
		this.fields.addAll(other.fields);
	}
	
}
