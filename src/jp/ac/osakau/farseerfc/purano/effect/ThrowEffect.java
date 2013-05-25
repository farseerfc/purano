package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@EqualsAndHashCode(callSuper=true)
public class ThrowEffect extends Effect<ThrowEffect> implements Cloneable{
	public ThrowEffect(DepSet deps, MethodRep from) {
		super(deps,from);
	}
	
	@NotNull
    @Override
	public ThrowEffect clone() {
		return new ThrowEffect(getDeps(), getFrom());
	}

	@Override
	protected List<String> dumpEffect(@NotNull MethodRep rep, @NotNull Types table) {
		return getDeps().dumpDeps(rep, table);
	}
}
