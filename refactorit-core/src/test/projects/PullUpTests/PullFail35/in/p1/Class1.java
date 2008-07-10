package p1;

public class Class1 {
	public void func2() { }
}

class Class2 extends Class1 {
// override
	public void func1() {
		func2();
	}

	public void func2() { }
}
