package p1;

interface InterFace1 {
// change access to public
	void f();
}

public class Class1 {
}

class Class2 implements InterFace1 {
// change access to public
	public void f() {
	}
}
