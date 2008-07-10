package p1;

public class A {

	static void f2() {
	}
}

class B {

// Move into B
// 1. change access of f2
// 2. move f2 also
// choose 1
	public static void f1() {
		A.f2();
	}
}
