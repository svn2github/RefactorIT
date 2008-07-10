
public class A {
}

class B {

// Move into B
// 1. change access of f2
// 2. move f2 also
// choose 2
	public static void f1() {
		f2();
	}

	private static void f2() {
	}
}
