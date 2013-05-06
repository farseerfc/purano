package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
public class NativeEffect extends Effect {
	public NativeEffect(DepSet deps) {
		super(deps);
	}

	public NativeEffect() {
		super(new DepSet());
	}
}
