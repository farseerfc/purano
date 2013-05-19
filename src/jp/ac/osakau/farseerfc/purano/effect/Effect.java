package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Escape;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(exclude="from")
public abstract class Effect implements Cloneable{
	private @Getter @Setter DepSet deps;
	private @Getter @Setter  MethodRep from;
	
	public Effect(DepSet deps, MethodRep from){
		this.deps = deps;
		this.from = from;
	}
	
	@Override
	public String toString(){
		return this.getClass().getName();
	}
	
	@Override
	public abstract Effect clone();
	
	public Effect duplicate(MethodRep from){
		Effect cl = clone();
		cl.setFrom(from);
		return cl;
	}
	
	public String dump(MethodRep rep, Types table, String prefix){
		String className = getClass().getSimpleName();
		className = className.substring(0,className.length() - 6 );
		String fromStr="";
		if(from != null){
			fromStr = Escape.from(" From "+from.toString(table));
		}
		return String.format("%s@%s(%s)%s",
				prefix,
				Escape.annotation(className),
				Escape.effect(dumpEffect(rep, table)),
				fromStr);
	}
	
	@NotNull
    public String dumpDot(){
		return "";
	}
	
	protected abstract String dumpEffect(MethodRep rep, Types table);
}
