package p1;

public class Class1 {
	private int t1;

	public void f1() {
	}
	public void f2() {
	}

	private int f3() {
		return 1;
	}
}

class Class2 extends Class1 {
// chain of uses
public void func1() {
	func2();
}
public void func2() {
	func3();
}
public void func3() {
	func4();
}
public void func4() {
	func5();
}
public void func5() {
}
}
