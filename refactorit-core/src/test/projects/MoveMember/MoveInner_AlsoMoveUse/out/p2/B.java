package p2;

import p1.A;


public class B {

	public int foo = 1;
	
	public void useInner() {
		Inner inner = new Inner();
		inner.useFoo();
	}
	
	public class Inner {
    int a = 0;
    
    public void useFoo() {
    	a += foo;
    }
  }
}