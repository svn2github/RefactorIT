package p1;

public class A {
// Move into X:
// 1. change access of f1
// 2. move f2 also
// 3. make f1 static
// always move
	private void f1() {
	}

	private void f2(A ref) {
		ref.f1();
	}
}
