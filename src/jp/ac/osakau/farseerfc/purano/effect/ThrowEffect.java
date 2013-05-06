package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
public class ThrowEffect extends Effect {
	public ThrowEffect(DepSet deps) {
		super(deps);
	}
}