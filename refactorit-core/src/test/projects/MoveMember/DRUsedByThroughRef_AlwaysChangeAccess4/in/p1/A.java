package p1;
import p2.X;

public class A extends X {
// Move into X:
// 1. change access of f1
// 2. move f also
// only change access
	void f1() {
	}
}

class B {
	private void f() {
		A refA = new A();
		refA.f1();
	}
}
