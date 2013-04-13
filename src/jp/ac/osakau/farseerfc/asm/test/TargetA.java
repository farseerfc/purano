package jp.ac.osakau.farseerfc.asm.test;

public class TargetA {
	private int c;
	
	public static int staticAdd(int a, int b){
		return a+b;
	}
	
	public int memberAdd(int a, int b){
		return a+b;
	}
	
	public int thisAdd(int a){
		return a+c;
	}
	
	public void setC(int c){
		this.c = c;
	}
	
}
