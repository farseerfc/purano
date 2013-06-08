package jp.ac.osakau.farseerfc.purano.test;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import jp.ac.osakau.farseerfc.purano.ano.CheckDependency;
import jp.ac.osakau.farseerfc.purano.ano.Return;

public class TargetC {
	private int member;
	
	@Override
	public  boolean equals(Object o){
		Integer mi = new Integer(12);
		member = mi.intValue();
		return false;
	}


    void setM(int v){
        member = v;
    }
}
