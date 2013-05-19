package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper=true)
public class OtherFieldEffect extends FieldEffect implements Cloneable {
	public OtherFieldEffect(String desc, String owner, String name,DepSet deps,DepSet leftValueDeps, MethodRep from) {
		super(desc,owner,name, null, from);
//		this.leftValueDeps = leftValueDeps;
	}
//	
//	private final @Getter DepSet leftValueDeps;
	
	@NotNull
    @Override
	public String getKey(){
		return getDesc()+getOwner()+getName();
	}
	
	@NotNull
    @Override
	public Effect clone() {
		return new OtherFieldEffect(getDesc(), getOwner(), getName(), getDeps(), null, getFrom());
	}

	@Override
	protected String dumpEffect(MethodRep rep, @NotNull Types table) {
		return String.format("%s %s#%s",
				table.desc2full(getDesc()),
				table.fullClassName(getOwner()),
				getName());
	}
}
