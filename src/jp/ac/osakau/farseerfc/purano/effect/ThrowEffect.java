package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.List;

public class ThrowEffect extends Effect<ThrowEffect> implements Cloneable{

	public ThrowEffect(DepSet deps, MethodRep from) {
		super(deps,from);
	}
	
	@NotNull
    @Override
	public ThrowEffect clone() {
		return new ThrowEffect( getDeps(), getFrom());
	}

	@NotNull
    @Override
	protected List<String> dumpEffect(@NotNull MethodRep rep, @NotNull Types table) {
		return getDeps().dumpDeps(rep, table);
	}


    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;


        return true;

    }

    @Override
    public int hashCode() {
        return 978126871;
    }
}
