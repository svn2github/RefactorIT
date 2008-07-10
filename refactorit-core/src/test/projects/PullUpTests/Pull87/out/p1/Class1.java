package p1;

public class Class1 {
	public void f(Class1 ref) {
	}
// this reference
	public void func1() {
		f(this);
	}
}

class Class2 extends Class1 {
}
