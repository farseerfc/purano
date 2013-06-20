package jp.ac.osakau.farseerfc.purano.test;


public class D {
    private static int s;

	private int c;

	public int thisAdd(int a){
		return a+c;
	}
	
	public void setC(int c){
		this.c = c;
	}
	
	public void change(){
		setC(1);
        s=2;
	}

	public void func(int a) {
		setC(a);
	}

    public void changeArg(D [] array){
        array[0].change();
    }


}
