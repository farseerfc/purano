package jp.ac.osakau.farseerfc.purano.test;


import org.jetbrains.annotations.NotNull;

public class D {
    private static int s;

	private int c;

	public D(int mc){
		this.c = mc;
	}

	public int thisAdd(int a){
		return a+c;
	}
	
	public void setC(int c){
		this.c = c;
	}
	
	public void allMember(){
		setC(1);
        s=2;
	}

	public void func(int a) {
		setC(a);
	}

	@NotNull
    public static D factory(int a){
		return new D(a);
	}
}
