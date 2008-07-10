package p1;

public class Class1 {
}

class Class2 {
// use of reference
	public int a;
	public void func1(Class2 ref) {
		ref.a = 1;
	}
}

class Class3 extends Class2 {
}
