package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
public class StaticFieldEffect extends FieldEffect implements Cloneable{
	public StaticFieldEffect(String desc,String owner,String name, DepSet deps, MethodRep from) {
		super(desc,owner,name,deps, from);
	}
	
	@Override
	public Effect clone(){
		return new StaticFieldEffect(getDesc(), getOwner(), getName(), getDeps(), getFrom());
	}

	@Override
	protected String dumpEffect(MethodRep rep, Types table) {
		return String.format("%s %s#%s: [%s]",
				table.desc2full(getDesc()),
				table.fullClassName(getOwner()),
				getName(), 
				getDeps().dumpDeps(rep,table));
	}
}
