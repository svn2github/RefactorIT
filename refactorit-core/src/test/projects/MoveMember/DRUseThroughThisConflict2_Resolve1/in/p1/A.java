package p1;

public class A {
// Move into B
// 1. change access of f2
// 2. move f2 also
// choose 1
	void f1() {
		f2();
	}

	private void f2() {
	}
}

class C extends A {
	private void f10() {
		B refB = new B();
		f1();
	}
}

class B {
	private void f10() {
		A refA = new A();
		refA.f1();
	}
}
