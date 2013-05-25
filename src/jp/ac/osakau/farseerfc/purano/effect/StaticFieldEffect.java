package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper=true)
public final class StaticFieldEffect extends FieldEffect<StaticFieldEffect> implements Cloneable{
	public StaticFieldEffect(String desc,String owner,String name, DepSet deps, MethodRep from) {
		super(desc,owner,name,deps, from);
	}
	
	@NotNull
    @Override
	public StaticFieldEffect clone(){
		return new StaticFieldEffect(getDesc(), getOwner(), getName(), getDeps(), getFrom());
	}

	@Override
	protected List<String> dumpEffect(@NotNull MethodRep rep, @NotNull Types table) {
        ArrayList<String> result = new ArrayList<String>(Arrays.asList(
                "type=" + table.desc2full(getDesc()),
                "owner=" + table.fullClassName(getOwner()),
                "name=\"" + getName() + "\""
        ));
        result.addAll(getDeps().dumpDeps(rep, table));
        return result;
	}
}
