package p1;

interface Interface1 {
	int b = 1;
	Class3 a = new Class3(1, b);
}

public class Class1 {
}

class Class2 implements Interface1 {
}

class Class3 {
	Class3(int a, int b) { }
}
