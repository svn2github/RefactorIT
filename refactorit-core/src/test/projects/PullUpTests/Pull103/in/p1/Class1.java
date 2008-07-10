package p1;

public class Class1 {
}

class Class2 extends Class1 {
// wrong order after pull up
	public int a = 1;
	public int b = a;
}
