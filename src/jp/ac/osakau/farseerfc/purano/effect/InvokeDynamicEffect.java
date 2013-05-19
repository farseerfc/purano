package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
public class InvokeDynamicEffect extends Effect implements Cloneable{
	public InvokeDynamicEffect(DepSet deps, MethodRep from) {
		super(deps, from);
	}

	public InvokeDynamicEffect(MethodRep from) {
		super(new DepSet(), from);
	}
	
	@Override
	public Effect clone() {
		return new InvokeDynamicEffect(getDeps(),getFrom());
	}

	@Override
	protected String dumpEffect(MethodRep rep, Types table) {
		return "";
	}
}
