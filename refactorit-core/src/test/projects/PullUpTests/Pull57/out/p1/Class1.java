package p1;

interface InterFace1 {
	int c;
	void f1();
// simple pull up
	void f();
}

public class Class1 {
}

class Class2 implements InterFace1 {
// simple pull up
	public void f() {
	}
}
