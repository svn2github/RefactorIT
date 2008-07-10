package p1;

public class Class1 {
// in the same package, no import is necessary
	public void func1() {
		new Class0();
	}
}

class Class2 extends Class1 {
}

class Class0 {
	public Class0() { }
}

