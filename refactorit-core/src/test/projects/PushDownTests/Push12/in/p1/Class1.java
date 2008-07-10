package p1;

public class Class1 {
}

class Class2 {
// cast this reference
	public void func1() {
		f(this, 1, this);
	}

	public void f(Class2 ref1, int a, Class2 ref2) {
	}

	public void f(Class3 ref1, int a, Class3 ref2) {
	}
}

class Class3 extends Class2
{
}
