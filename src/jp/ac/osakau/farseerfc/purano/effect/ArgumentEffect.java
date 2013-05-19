package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper=true)
public class ArgumentEffect extends Effect implements Cloneable {

	private final @Getter int argPos;
	
	public ArgumentEffect(int argPos,DepSet deps, MethodRep from) {
		super(deps,from);
		this.argPos = argPos;
	}
	
	@Override 
	public Effect clone() {
		return new ArgumentEffect(argPos, getDeps(), getFrom());
	}

	@Override
	public String dumpEffect(MethodRep rep, Types table) {
		if(getArgPos() < rep.getMethodNode().localVariables.size()){
			return String.format("%s:[%s]",
					rep.getMethodNode().localVariables.get(getArgPos()).name,
					getDeps().dumpDeps(rep, table)
					);
		}else{
			return String.format("#%d:[%s]",
					getArgPos(),
					getDeps().dumpDeps(rep, table)
					);
		}
	}

}
