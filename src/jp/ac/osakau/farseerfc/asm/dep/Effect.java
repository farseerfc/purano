package jp.ac.osakau.farseerfc.asm.dep;

import lombok.Data;

public abstract @Data class Effect {
	private final DepSet deps;
	
	public String toString(){
		return this.getClass().getName();
	}
}
