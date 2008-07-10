package p1;

public class A {

	void f2() {
	}
}

class C extends A {
	private void f10() {
		B refB = new B();
		refB.f1(this);
	}
}

class B {
	private void f10() {
		A refA = new A();
		f1(refA);
	}

// Move into B
// 1. change access of f2
// 2. move f2 also
// choose 1
	void f1(A a) {
		a.f2();
	}
}
