package jp.ac.osakau.farseerfc.purano.dep;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

public @Data class DepSet {
	private final Set<FieldDep> statics = new HashSet<>();
	// this pointer, Args, and true locals, all in the locals list, by the order
	private final Set<Integer> locals = new HashSet<>();
	
	private final Set<FieldDep> fields = new HashSet<>();
	
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
