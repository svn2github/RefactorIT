package p1;

public class Class1 {
}

class Class2 {
// cast this reference
	public int a = f(this, 1, this);

	public int f(Class2 ref1, int a, Class2 ref2) {
		return 1;
	}

	public int f(Class3 ref1, int a, Class3 ref2) {
		return 2;
	}
}

class Class3 extends Class2
{
}
