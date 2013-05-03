package jp.ac.osakau.farseerfc.purano.test;

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
	
	
	public static void main(String [] args) throws ClassNotFoundException{
		Class c = Class.forName("[Ljava.lang.Class;");
		int [] a = new int [4];
		
		System.out.println(c.getDeclaredMethods());
	}
}
