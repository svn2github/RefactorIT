package p1;
import p2.InterFace1;

public class Class1 {
	static final int b = 1;
}

class Class2 implements InterFace1 {
// import not possible
	public int a = Class1.b;
}
