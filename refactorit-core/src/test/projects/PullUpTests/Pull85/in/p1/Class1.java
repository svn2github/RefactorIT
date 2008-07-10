package p1;

public class Class1 {
}

class Class2 extends Class1 {
// this reference
	public void func1() {
		f(this);
	}

	public void f(Class2 ref) {
	}
}
