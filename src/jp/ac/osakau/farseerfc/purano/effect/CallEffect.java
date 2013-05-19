package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper=true)
public class CallEffect extends FieldEffect implements Cloneable{
	public CallEffect(String callType,String desc,String owner,String name, DepSet deps, MethodRep from) {
		super(desc,owner,name,deps,from);
		this.callType = callType;
	}
	
	private final @Getter String callType;

	@NotNull
    @Override
	public Effect clone(){
		return new CallEffect(callType, getDesc(),getOwner(),getName(), getDeps(), getFrom());
	}

	@Override
	protected String dumpEffect(@NotNull MethodRep rep, @NotNull Types table) {
		return String.format("%s %s: [%s]",
				getCallType(),
				table.dumpMethodDesc(getDesc(),
						String.format("%s#%s",
								table.fullClassName(getOwner()), 
								getName())),
								getDeps().dumpDeps(rep ,table));
	}

}
