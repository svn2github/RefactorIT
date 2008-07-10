package p1;

public class Class1 {
}

class Class2 extends Class1 {
// double resolve
	public void func1() {
		func2();
	}

	private void func2() {
	}

	private void func3() {
		func2();
	}
}
