package jp.ac.osakau.farseerfc.purano.test;

public class ForLoopTest {

	public void forloop(){
		int [] arr = new int [100];
		
		for(int i=0; i<100; ++i){
			arr[i] = arr[i]*2;
		}
	}
	
	public void forEachLoop(){
		String [] result = new String [100]; 
		
		for(String s : result){
			s.toUpperCase();
		}
	}
}
