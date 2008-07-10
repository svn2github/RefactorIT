package p1;

public class A {
// used by native
// import of target into native not possible
// native method has no target instance
// native cannot be moved also
	public void f1() {
	}

	public void f2() {
		B refB;
		f1();
	}	
}

class B {
}
