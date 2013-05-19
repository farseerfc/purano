package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper=true)
public class ThisFieldEffect extends FieldEffect implements Cloneable{
	public ThisFieldEffect(String desc,String owner,String name, DepSet deps, MethodRep from) {
		super(desc,owner,name,deps,from);
	}
	
	@NotNull
    @Override
	public Effect clone(){
		return new ThisFieldEffect(getDesc(), getOwner(), getName(), getDeps(), getFrom());
	}

	@Override
	protected String dumpEffect(@NotNull MethodRep rep, @NotNull Types table) {
		return String.format("%s %s#this.%s: [%s]",
				table.desc2full(getDesc()),
				table.fullClassName(getOwner()),
				getName(), 
				getDeps().dumpDeps(rep,table));
	}
}
