package p1;

interface InterFace1 {
	int c;
	int a = Class2.b;
	void f();
}

public class Class1 {
}

class Class2 implements InterFace1 {
// final initalizer
	public static final int b = 1;
}
