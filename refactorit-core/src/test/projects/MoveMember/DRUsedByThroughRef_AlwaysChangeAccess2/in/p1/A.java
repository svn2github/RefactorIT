package p1;
import p2.X;

public class A extends X {
// Move into X:
// 1. change access of f1
// 2. move f2 also
// always change access, not possible to call everywhere
	void f1() {
	}

	private void f2(A refA) {
		refA.f1();
	}
}

class B {
	private void f3() {
		A refA = new A();
		refA.f1();
	}
}
