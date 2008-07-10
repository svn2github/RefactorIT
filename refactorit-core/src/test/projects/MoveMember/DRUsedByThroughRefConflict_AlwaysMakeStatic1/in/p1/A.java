package p1;

public class A {
// Move into X
// always make static, since f2 has no target instance, f2 cannot be moved also
// since B cannot be imported
	private void f1() {
	}

	private void f2() {
		B refB = new B();
		f1();
	}
}

class B {
}
