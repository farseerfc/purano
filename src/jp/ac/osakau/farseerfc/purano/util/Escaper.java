package jp.ac.osakau.farseerfc.purano.util;

public abstract class Escaper {
	public static boolean use = true;

	public abstract String className(String name);

	public abstract String methodName(String name);

	public abstract String effect(String str);
	
	public abstract String from(String str);
	
	public abstract String annotation(String str);

    public abstract String purity(String str);
    
    public static Escaper getDummy(){
    	return new DummyEscaper();
    }
    
    public static Escaper getTerm(){
    	return new TermEscaper();
    }
    
    public static Escaper getHtml(){
    	return new HtmlEscaper();
    }
}
