package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import lombok.Data;

public abstract @Data class Effect {
	private final DepSet deps;
	
	@Override
	public String toString(){
		return this.getClass().getName();
	}
}
