package p1;
import p2.Class10;

public class Class2 extends Class10 {
// old type reference, move since member is invoked without reference also
	public void func1(Class2 ref) {
		ref.func2();

		func3();
	}

	public void func2() { }

	public void func3() {
		func2();
	}
}
