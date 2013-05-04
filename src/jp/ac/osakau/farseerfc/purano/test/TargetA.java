package jp.ac.osakau.farseerfc.purano.test;


public class TargetA implements TargetInterface{
	private int c;
	private int [] ma;
	
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
	
	public void allMember(){
		setC(12);
	}

	@Override
	public void func(int a) {
		setC(a);
	}
	

	public void localArrayAccess(){
		int [] a = new int [12];
		a[0]=1;
	}
	
	public void mArrayNew(){
		ma = new int [c];
	}
	
	public void mArrayAccess(){
		ma[1] = c;
	}
	
	public void argArrayAccess(int a []){
		a[1]= c;
	}
}
