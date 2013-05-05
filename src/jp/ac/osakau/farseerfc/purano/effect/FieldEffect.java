package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper=true)
public abstract class FieldEffect extends Effect {
	public FieldEffect(String desc,String owner,String name, DepSet deps) {
		super(deps);
		this.desc = desc;
		this.owner = owner;
		this.name = name;
	}
	
	private final @Getter String desc;
	private final @Getter String owner;
	private final @Getter String name;
}
