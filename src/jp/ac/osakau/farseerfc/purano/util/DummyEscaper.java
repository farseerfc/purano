package jp.ac.osakau.farseerfc.purano.util;

public class DummyEscaper extends Escaper {

	public String className(String name) {
		return name;	
	}

	public String methodName(String name) {	
		return name;
	}

	public String effect(String str) {
		return str;
	}
	
	public String from(String str){
		return str;
	}
	
	public String annotation(String str){
		return str;
	}

    public String purity(String str){
        return str;
    }
}
