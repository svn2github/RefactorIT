package p1;
import p2.X;

public class A {

	private void f2(A refA) {
		X refX = new X();
		refX.f1();
	}
}

class B {
	private void f3() {
		X refX = new X();
		A refA = new A();
		refX.f1();
	}
}
