package p1;

public class A {
	public A() {
		f2();
	}

// Move into X
// 1. change access of f2
// 2. move f2 also
// always change access of f2, since it is not possible to move f2
// since it is used by A.A() and f2 cannot be made static
// because target cannot be imported
	private void f1() {
		f2();
	}

	private void f2() { }
}
