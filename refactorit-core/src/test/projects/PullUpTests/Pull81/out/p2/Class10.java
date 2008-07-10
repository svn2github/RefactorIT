package p2;

public class Class10 {
// old type reference, move since member is invoked without reference also;
// double resolve 1: change access
	public void func1(Class10 ref) {
		ref.func2();

		func3();
	}

	protected void func2() { }

	public void func3() {
		func2();
	}
}
