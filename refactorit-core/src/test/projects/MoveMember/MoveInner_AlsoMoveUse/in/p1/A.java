package p1;

public class A {
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