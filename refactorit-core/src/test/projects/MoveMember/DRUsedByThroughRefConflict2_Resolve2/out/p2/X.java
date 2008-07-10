package p2;

public class X {

// Move into X:
// 1. change access of f1
// 2. move f2 also
// choose 2
	private void f1() {
	}

	private void f2() {
		f1();
	}
}
