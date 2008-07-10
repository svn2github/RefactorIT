package p1;

public class Class1 {
	public void f1() { }
}

interface Interface1 {
	int a = 1;
	int b = a;
}

class Class2 extends Class1 implements Interface1 {

	public void f1() {
	}
}
