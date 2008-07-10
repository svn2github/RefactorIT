package p1;

public class A {
// Move into X:
// 1. change access of f3, f4
// 2. move f3,f4 also
// choose 2
	private void f1(A ref) {
		ref.f3();
		ref.f4();
		ref.f5();
	}

	private void f3() { }

	protected void f4() { }

	public void f5() { }
}
