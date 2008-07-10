package p1;
import p2.Interface1;

public class Class1 {
}

class Class2 implements Interface1 {
// add static init
	public int a = Class3.b;
}

class Class3 {
	static int b = 1;
}
