package p1;
import p2.X;

public class A extends X {

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
