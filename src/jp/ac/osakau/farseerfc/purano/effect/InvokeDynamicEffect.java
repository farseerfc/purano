package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper=true)
public class InvokeDynamicEffect extends Effect<InvokeDynamicEffect> implements Cloneable{
	public InvokeDynamicEffect(DepSet deps, MethodRep from) {
		super(deps, from);
	}

	public InvokeDynamicEffect(MethodRep from) {
		super(new DepSet(), from);
	}
	
	@NotNull
    @Override
	public InvokeDynamicEffect clone() {
		return new InvokeDynamicEffect(getDeps(),getFrom());
	}

	@NotNull
    @Override
    protected List<String> dumpEffect(MethodRep rep, Types table) {
        return new ArrayList<>();
	}
}
