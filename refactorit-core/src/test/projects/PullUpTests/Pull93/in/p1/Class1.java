package p1;

interface InterFace1 {
}

public class Class1 {
}

class Class2 implements InterFace1 {
	public int b = 1;
// move b also
	public int a = b;
}
