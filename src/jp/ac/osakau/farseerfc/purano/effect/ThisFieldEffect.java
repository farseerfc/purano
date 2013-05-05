package jp.ac.osakau.farseerfc.purano.effect;

import jp.ac.osakau.farseerfc.purano.dep.DepSet;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=true)
public class ThisFieldEffect extends FieldEffect{
	public ThisFieldEffect(String desc,String owner,String name, DepSet deps) {
		super(desc,owner,name,deps);
	}
}
