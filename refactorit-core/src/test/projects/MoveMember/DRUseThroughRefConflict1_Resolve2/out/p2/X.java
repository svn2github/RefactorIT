package p2;

import p1.A;


public class X {

// Move into X:
// 1. change access of f3, f4
// 2. move f3,f4 also
// choose 2
	private void f1(A ref) {
		f3();
		f4();
		ref.f5();
	}

	private void f3() { }

	protected void f4() { }
}
