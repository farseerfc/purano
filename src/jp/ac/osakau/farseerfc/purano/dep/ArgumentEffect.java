package jp.ac.osakau.farseerfc.purano.dep;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper=true)
public class ArgumentEffect extends Effect {

	private final @Getter int argPos;
	
	public ArgumentEffect(int argPos,DepSet deps) {
		super(deps);
		this.argPos = argPos;
	}

}
