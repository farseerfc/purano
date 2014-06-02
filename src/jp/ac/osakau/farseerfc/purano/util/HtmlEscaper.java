package jp.ac.osakau.farseerfc.purano.util;

public class HtmlEscaper extends Escaper {

	public String className(String name) {
		return "<span style=\"color:blue\">"+name+"</span>";	
	}

	public String methodName(String name) {	
		return "<span style=\"color:green\">"+name+"</span>";	
	}

	public String effect(String str) {
		return "<span style=\"color:red\">"+str+"</span>";	
	}
	
	public String from(String str){
		return "<span style=\"color:red\">"+str+"</span>";	
	}
	
	public String annotation(String str){
		return "<span style=\"color:brown\">"+str+"</span>";	
	}

    public String purity(String str){
    	return "<span style=\"color:grey\">"+str+"</span>";	
    }
}
