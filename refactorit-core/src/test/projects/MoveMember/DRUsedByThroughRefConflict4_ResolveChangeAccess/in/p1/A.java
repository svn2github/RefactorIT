package p1;
import p2.X;

public class A {
// Move into X:
// 1. change access of f1
// 2. move f2 also
// 3. make f1 static
// 2 is not possible since f2 is called by B.f, choose 1
	void f1() {
	}

	public void f2(A refA) {
		X refX = new X();
		refA.f1();
	}
}

class B {
	private void f() {
		A refA = new A();
		refA.f2(null);
	}
}
