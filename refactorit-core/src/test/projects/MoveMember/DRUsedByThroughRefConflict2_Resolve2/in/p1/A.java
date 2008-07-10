package p1;
import p2.X;

public class A extends X {
// Move into X:
// 1. change access of f1
// 2. move f2 also
// choose 2
	private void f1() {
	}

	private void f2(A ref) {
		ref.f1();
	}
}
