package p1;

public class Class1 {
	public void f1() { }
}

interface Interface1 {
}

class Class2 extends Class1 implements Interface1 {
	public int a = 1;
	public int b = a;

	public void f1() {
	}
}
