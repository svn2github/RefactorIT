package p1;
import p2.Class10;

public class Class2 extends Class10 {
// old type reference, move since member is invoked without reference also;
// double resolve 1: change access
	public void func1(Class2 ref) {
		ref.func2();

		func3();
	}

	private void func2() { }

	public void func3() {
		func2();
	}

	public void func4() {
		func2();
	}
}
