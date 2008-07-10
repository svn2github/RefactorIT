package p1;

public class A {
}

class B extends A {

// Move into B
// 1. change access of f2
// 2. move f2 also
// choose 2
	private void f1() {
		f2();
	}

	private void f2() {
	}
}
