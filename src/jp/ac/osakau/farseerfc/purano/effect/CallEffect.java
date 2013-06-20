package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper=true)
public class CallEffect extends AbstractFieldEffect<CallEffect> implements Cloneable{
	public CallEffect(String callType, String desc, String owner, String name, DepSet deps, MethodRep from) {
		super(desc,owner,name,deps,from);
		this.callType = callType;
	}
	
	private final @Getter String callType;

	@NotNull
    @Override
	public CallEffect clone(){
		return new CallEffect(callType, getDesc(),getOwner(),getName(), getDeps(), getFrom());
	}

	@NotNull
    @Override
	protected List<String> dumpEffect(@NotNull MethodRep rep, @NotNull Types table) {
        ArrayList<String> result = new ArrayList<>(Arrays.asList(
                "callType=\""+getCallType()+"\"",
                "method=\""+
                        table.dumpMethodDesc(getDesc(),
                        String.format("%s#%s",
                                table.fullClassName(getOwner()),
                                getName())) +"\""
        ));
        result.addAll(getDeps().dumpDeps(rep, table));
        return result;
	}

}
