package p2;

import p1.A;


public class X {

// Move into X:
// 1. change access of f3, f4
// 2. move f3,f4,f5 also
// choose 1
	private void f1(A ref) {
		ref.f3();
		ref.f4();
		ref.f5();
	}
}
