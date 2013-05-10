package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
public class OtherFieldEffect extends FieldEffect {
	public OtherFieldEffect(String desc, String owner, String name,DepSet deps,DepSet leftValueDeps) {
		super(desc,owner,name, null);
//		this.leftValueDeps = leftValueDeps;
	}
//	
//	private final @Getter DepSet leftValueDeps;
	
	@Override
	public String getKey(){
		return getDesc()+getOwner()+getName();
	}
	
	
}
