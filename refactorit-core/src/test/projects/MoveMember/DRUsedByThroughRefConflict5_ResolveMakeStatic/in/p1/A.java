package p1;

import p2.X;

public class A {
// Move into X
// 1. change access of f1 and call through refx
// 2. move f2 also
// 3. make f1 static
// choose 3
	private void f1() {
	}

	private void f2() {
		X refX = new X();
		f1();
	}
}
