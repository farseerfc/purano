package jp.ac.osakau.farseerfc.purano.test;

public class TargetC {
	private int member;
	
	@Override
	public  boolean equals(Object o){
		member = 1;
		return false;
	}
}
