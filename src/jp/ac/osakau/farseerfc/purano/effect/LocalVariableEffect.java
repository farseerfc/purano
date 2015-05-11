package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import jp.ac.osakau.farseerfc.purano.util.Types;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

//@EqualsAndHashCode(callSuper=false)
public class LocalVariableEffect extends Effect<LocalVariableEffect> {

	private final @Getter int argPos;
	
	public LocalVariableEffect(int argPos,DepSet deps, MethodRep from) {
		super(deps,from);
		this.argPos = argPos;
	}
	
	@NotNull
    @Override
	public LocalVariableEffect clone() {
		return new LocalVariableEffect(argPos, getDeps(), getFrom());
	}

	@NotNull
    @Override
	public List<String> dumpEffect(@NotNull MethodRep rep, @NotNull Types table) {
		if(getArgPos() < rep.getMethodNode().localVariables.size()){
            ArrayList<String> result = new ArrayList<>();
            result.add("name=\""+rep.getMethodNode().localVariables.get(getArgPos()).name+"\"");
            result.addAll(getDeps().dumpDeps(rep, table));
			return result;

		}else{
            ArrayList<String> result = new ArrayList<>();
            result.add("name=#\""+getArgPos()+"\"");
            result.addAll(getDeps().dumpDeps(rep, table));
            return result;
		}
	}
	
	@Override
	public String toString(){
		return String.format("LocalVariableEffect: %d", argPos);
	}

	@Override
	public boolean equals(Object other){
		if(other instanceof LocalVariableEffect){
			return equals((LocalVariableEffect)other);
		}
		return false;
	}
	
	public boolean equals(LocalVariableEffect other){
		return argPos == other.argPos;
	}
	
	@Override
	public int hashCode(){
		return argPos*61;
	}
}
