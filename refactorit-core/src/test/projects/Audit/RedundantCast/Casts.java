import java.util.*;

class A {

	A(){
	
	}
	
	A(A c){
	
	}

}

class B extends A{
	
	/**
	 * @audit RedundantCast
	 */
	B (){
	
		super((A) new C());
	
	}

}

class C  extends A{

	C(A something) {
	
	}

	/**
	 * @audit RedundantCast
	 */	
	C(){
		this((D) new D());
	
	}
}

class D extends A{


}

class MyClass {
	private int density;

	public MyClass (A a, int b){
	
	
	}
		
	public MyClass(){
	
	
	}

	/**
	 *
	 */		
	public void setDensity(int density){
	
		this.density = density;
	}

}

public class Casts {
	

	/**
	 *
	 */			
	public static void main(String[] args) {
	
	   byte hm = 33;
	   
	   System.out.println("Some string, " + hm + ", and another string");
   	   System.out.println("Some string, " + (char) hm + ", and another string");
	   
	   test4();
	
	
	}

	/**
	 *
	 */			
	public void test(){
	
	List list = new ArrayList();
	list.add(new MyClass());
	
	for (Iterator i = list.iterator(); i.hasNext(); ) {
          ((MyClass) i.next()).setDensity(5);
        }
	
	for (Iterator i = list.iterator(); i.hasNext(); ){
	   MyClass clas = (MyClass) i.next();
	   clas.setDensity(5);
	
	}
	
	for (Iterator i = list.iterator(); i.hasNext(); ){
	   MyClass clas;
	   clas = (MyClass) i.next();
	   clas.setDensity(5);
	
	}
	
	
	}

	/**
	 *
	 */			
	public void test2(){
		byte i = 1;
		byte k = 4;
		String a = "" + (char)i + k;

	
	}
	
	/**
	 * @audit RedundantCast
	 */			
	public void test3(){
		int a = 4;
		float b = (float) a++;
	}

	/**
	 * @audit RedundantCast
	 * @audit RedundantCast	 
	 */	
	public static void test4(){
	
		Integer a = new Integer(5);
		Integer b = new Integer((int) 'A');
		char c = 'L';
		Integer d = new Integer((int) c);
		System.out.println("with cast: " + d);
		Integer e = new Integer(c);
		System.out.println("without cast: " + e);
	
	}

	/**
	 * @audit RedundantCast
	 */	
	public static void test5(){
	
		A b = new B();
		A c = new C();
		
		MyClass myclassB = new MyClass((B) b, 5);
		//MyClass myclassC = new MyClass(c, 5);
		
	}

	/**
	 *
	 */			
	public void setSmth( A a ) {
	
	
	}

	/**
	 * @audit RedundantCast
 	 * @audit RedundantCast
	 */	
	public void test6(){
	
		setSmth((A) new D());
		setSmth((D) new D());
	}

	/**
	 *
	 */		
	public void test7(){
		long b = 5;
		int[] c = new int[(int) b];
	
	}

	/**
	 * @audit RedundantCast
	 */	
	public Object test8(){
		
		A d = new D();
		
		return (Object) d;
	}
	
}
