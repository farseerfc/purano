package test;
public class HelloWorld {
	
	public static void main(String[] args) {
		int [] a= new int [1];
		a[0] = 12;
		int [] b = a;
		(a= new int [1])[0]=13;
		System.out.println(b[0]);
		System.out.println(a[0]);
		
	}
}
