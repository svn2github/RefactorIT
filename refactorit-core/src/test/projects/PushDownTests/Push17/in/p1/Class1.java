package p1;

public class Class1 {
}

class Class2 {
// double resolve 2
	private int a;

	public void func1() {
	}

	public void func2(Class2 ref) {
		ref.a = 1;
		func1();
	}
}

class Class3 extends Class2 {
}
