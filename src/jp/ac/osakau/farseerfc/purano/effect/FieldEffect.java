package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import jp.ac.osakau.farseerfc.purano.reflect.MethodRep;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper=true)
public abstract class FieldEffect extends Effect {
	public FieldEffect(String desc,String owner,String name, DepSet deps, MethodRep from) {
		super(deps,from);
		this.desc = desc;
		this.owner = owner;
		this.name = name;
		
	}
	
	private final @Getter String desc;
	private final @Getter String owner;
	private final @Getter String name;
	
	public String getKey(){
		return desc+owner;//+name;
	}
}
