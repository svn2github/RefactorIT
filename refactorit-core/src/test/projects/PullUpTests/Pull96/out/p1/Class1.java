package p1;

public class Class1 {
}

class Class2 implements InterFace1 {
	public void f() {
	}
}

class Class3 implements InterFace1 {

	public void f() {
		throw new RuntimeException("method f is not implemented");
	}
}

class Class4 extends Class3 {
}
