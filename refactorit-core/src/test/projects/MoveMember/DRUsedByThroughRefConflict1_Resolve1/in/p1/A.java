package p1;
import p2.X;

public class A {
// Move into X:
// 1. change access of f1
// 2. move f2 also
// choose 1
	private void f1() {
	}

	private void f2(A ref) { 
		X refX = new X();
		ref.f1();
	}
}
