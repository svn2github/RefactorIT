package p1;

public class Class1 {
	public void f(Class1 ref) {
	}

	public void f(Class2 ref) {
	}
// this reference
	public void func1() {
		f((Class1)this);
	}
}

class Class2 extends Class1 {
}
