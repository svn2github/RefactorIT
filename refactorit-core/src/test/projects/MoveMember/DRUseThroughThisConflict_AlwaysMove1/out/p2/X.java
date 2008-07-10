package p2;

public class X {

// Move into X
// 1. change access of f2
// 2. move f2 also
// cannot import A into X, choose always 2
	public void f1() {
		f2();
	}

	public void f2() {
	}
}
