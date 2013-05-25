package jp.ac.osakau.farseerfc.purano.effect;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Escape;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(exclude="from")
public abstract class Effect<T extends Effect> implements Cloneable{
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
	
	@NotNull
    @Override
	public abstract T clone();
	
	@NotNull
    public T duplicate(MethodRep from){
		T cl = clone();
		cl.setFrom(from);
		return cl;
	}
	
	public String dump(MethodRep rep, @NotNull Types table, String prefix){
		String className = getClass().getSimpleName();
		className = className.substring(0,className.length() - 6 );
		String fromStr="";
		if(from != null){
			fromStr = Escape.from("inheritedFrom = \""+
                table.dumpMethodDesc(from.getInsnNode().desc,
                    String.format("%s#%s",
                        table.fullClassName(from.getInsnNode().owner),
                        from.getInsnNode().name))+"\"");
		}
        ArrayList<String> result = new ArrayList<>(Lists.transform(dumpEffect(rep, table),new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable String s) {
                return Escape.effect(s);
            }
        }));
        result.add(fromStr);
		return String.format("%s@%s(%s)",
				prefix,
				Escape.annotation(className),
                Joiner.on(", ").join(result));
	}
	

	protected abstract List<String> dumpEffect(MethodRep rep, Types table);
}
