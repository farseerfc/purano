package jp.ac.osakau.farseerfc.purano.dep;

import lombok.Getter;

public class ArgumentEffect extends Effect {

	private final @Getter int argPos;
	
	public ArgumentEffect(int argPos,DepSet deps) {
		super(deps);
		this.argPos = argPos;
	}

}
