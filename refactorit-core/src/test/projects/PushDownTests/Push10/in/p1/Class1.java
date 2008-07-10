package p1;

public class Class1 {
}

class Class2 {
// double resolve
	public void func1() { }

	public void func2() { 
		func1();
		func3();
	}

	private void func3() { }
}

class Class3 extends Class2 {
}
