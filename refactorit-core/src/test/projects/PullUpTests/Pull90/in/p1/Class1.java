package p1;

public class Class1 {
}

class Class2 extends Class1 {
// this reference
	public int a = f(this);

	public int f(Class2 ref) {
		return 1;
	}
}
