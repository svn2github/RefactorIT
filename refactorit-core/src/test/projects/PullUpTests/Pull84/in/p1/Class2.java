package p1;
import p2.Class10;

public class Class2 extends Class10 {
	Class2() {
		func2();
	}

// move func2, since func3 uses it, change access since constructor uses it
	public void func1(Class2 ref) {
		ref.func2();

		func3();
	}

	private void func2() { }

	public void func3() {
		func2();
	}
}
