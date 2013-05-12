package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
public class ThrowEffect extends Effect implements Cloneable{
	public ThrowEffect(DepSet deps, MethodRep from) {
		super(deps,from);
	}
	
	@Override
	public Effect clone() {
		return new ThrowEffect(getDeps(), getFrom());
	}

	@Override
	protected String dumpEffect(MethodRep rep, Types table) {
		return getDeps().dumpDeps(rep, table);
	}
}
