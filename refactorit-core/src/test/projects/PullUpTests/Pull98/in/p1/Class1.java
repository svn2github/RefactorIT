package p1;

interface Interface1 {
}

public class Class1 {
}

class Class2 implements Interface1 {
	public int b = 1;
	public Class3 a = new Class3(1, b);
}

class Class3 {
	Class3(int a, int b) { }
}
