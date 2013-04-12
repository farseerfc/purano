package jp.ac.osakau.farseerfc.asm.dep;

import lombok.Getter;

public class CallEffect extends FieldEffect{
	public CallEffect(String callType,String desc,String owner,String name, DepSet deps) {
		super(desc,owner,name,deps);
		this.callType = callType;
	}
	
	private final @Getter String callType;
}
