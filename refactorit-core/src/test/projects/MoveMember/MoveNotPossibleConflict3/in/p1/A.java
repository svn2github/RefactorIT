package p1;

public class A {
// used by not native
// import of target into native not possible
// not native method has no target instance
	public void f1() {
	}
}

class B {
	private void f() {
		A refA = new A();
		refA.f1();
	}
}
