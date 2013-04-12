package jp.ac.osakau.farseerfc.asm.dep;

import lombok.Getter;

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
