package jp.ac.osakau.farseerfc.purano.test;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

public class TargetC {
	private int member;
	
	@Override
	public  boolean equals(Object o){
		Integer mi = new Integer(12);
		member = mi.intValue();
		return false;
	}
	
	public boolean isPublic(Object o) {
		Integer accessFlags = Integer.valueOf(
				(o instanceof FieldInfo) 
				? ((FieldInfo) o).getAccessFlags()
				: (o instanceof ClassFile) 
					? ((ClassFile) o).getAccessFlags()
					: ((o instanceof MethodInfo) 
							? Integer.valueOf(((MethodInfo) o).getAccessFlags())
							: null)
						.intValue()
				);

		return ((accessFlags != null) && (
				AccessFlag.isPublic(accessFlags.intValue())));
	}

	public static void main(String [] args) throws InterruptedException{
		Integer i = null;
		System.out.println(i.intValue());
	}
}
