package p1;

public class Class1 {
}

class Class2 {
// double resolve 2
	int a;
}

class Class3 extends Class2 {

	public void func1() {
	}

	public void func2(Class2 ref) {
		ref.a = 1;
		func1();
	}
}
