package p2;

public class Class10 {
// old type reference, move since member is invoked without reference also
	public void func1(Class10 ref) {
		ref.func2();

		func3();
	}

	public void func2() { }

	public void func3() {
		func2();
	}
}
