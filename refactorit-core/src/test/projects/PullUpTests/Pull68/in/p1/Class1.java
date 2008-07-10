package p1;

public class Class1 {
}

class Class2 implements InterFace1 {
	public void f() {
	}
}

class Class3 implements InterFace1 {
}

interface InterFace2 extends InterFace1 {
}

class Class4 implements InterFace2 {
}

interface InterFace3 extends InterFace2 {
}

class Class5 implements InterFace3 {
}

class Class6 implements InterFace1 {
	public void f() {
	}
}
