package p1;

public class Class1 {
// double resolve
	public void func1() {
		func2();
	}

	void func2() {
	}
}

class Class2 extends Class1 {

	private void func3() {
		func2();
	}
}
