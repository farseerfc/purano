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
public class OtherFieldEffect extends FieldEffect<OtherFieldEffect> implements Cloneable {
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
	public OtherFieldEffect clone() {
		return new OtherFieldEffect(getDesc(), getOwner(), getName(), getDeps(), null, getFrom());
	}

	@Override
	protected List<String> dumpEffect(MethodRep rep, @NotNull Types table) {
        ArrayList<String> result = new ArrayList<>(Arrays.asList(
                "type="+table.desc2full(getDesc()),
                "owner="+table.fullClassName(getOwner()),
                "name=\""+getName()+"\""
        ));
        return result;
	}
}
