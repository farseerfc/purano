package jp.ac.osakau.farseerfc.purano.dep;

import lombok.Getter;


public class OtherFieldEffect extends FieldEffect {
	public OtherFieldEffect(String desc, String owner, String name,DepSet deps,DepSet leftValueDeps) {
		super(desc,owner,name,deps);
		this.leftValueDeps = leftValueDeps;
	}
	
	private final @Getter DepSet leftValueDeps;
}
