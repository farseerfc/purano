package jp.ac.osakau.farseerfc.purano.test;

public class TargetC {
	private int member;
	
	@Override
	public  boolean equals(Object o){
		member = 1;
		return false;
	}
	
	public static void main(String [] args) throws InterruptedException{
		for(int i=1;i<100;++i){
			System.out.print("\rWaiting ... "+i+"%");
			System.out.flush();
			Thread.sleep(50);
		}
		System.out.println();
	}
}
