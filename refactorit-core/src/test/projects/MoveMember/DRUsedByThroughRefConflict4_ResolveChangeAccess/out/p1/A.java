package p1;
import p2.X;

public class A {

	public void f2(A refA) {
		X refX = new X();
		refX.f1();
	}
}

class B {
	private void f() {
		A refA = new A();
		refA.f2(null);
	}
}
