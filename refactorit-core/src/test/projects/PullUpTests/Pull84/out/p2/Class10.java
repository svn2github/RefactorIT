package p2;

public class Class10 {

// move func2, since func3 uses it, change access since constructor uses it
	public void func1(Class10 ref) {
		ref.func2();

		func3();
	}

	protected void func2() { }

	public void func3() {
		func2();
	}
}
