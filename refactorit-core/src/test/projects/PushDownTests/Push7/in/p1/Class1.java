package p1;

public class Class1 {
}

class Class2 {
// sub invokes the func1, but func1 is overriden
	public void func1() {  }
}

class Class3 extends Class2 {
}

class Class4 extends Class2 {
	public void func1() { }

        Class4() {
		func1();
	}
}

