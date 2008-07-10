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

interface InterFace2 extends InterFace1 {
}

class Class4 implements InterFace2 {

	public void f() {
		throw new RuntimeException("method f is not implemented");
	}
}

interface InterFace3 extends InterFace2 {
}

class Class5 implements InterFace3 {

	public void f() {
		throw new RuntimeException("method f is not implemented");
	}
}

class Class6 implements InterFace1 {
	public void f() {
	}
}
