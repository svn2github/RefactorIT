package p1;

public class Class1 {
}

class Class2 extends Class3 {
// check if foreign method is accessible after pull up
	public void func1() {
		new Class0().f();
	}
}

class Class0 {
	public Class0() { }
	void f() { }
}

