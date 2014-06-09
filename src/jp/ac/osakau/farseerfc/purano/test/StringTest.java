package jp.ac.osakau.farseerfc.purano.test;

public class StringTest {
	private int hash;
	private final String value;
	
	public StringTest(String value){
		this.value = value;
		this.hash = 0;
	}

	public int hashCode(){
		if(hash == 0){
			hash = value.hashCode();
		}
		return hash;
	}
}
