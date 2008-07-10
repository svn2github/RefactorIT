package p1;

public class Class1 {
}

class Class2 {
// sub uses pushed down conflict
	public void func1() { }
}

class Class3 extends Class2 {
}

class Class4 extends Class2 {
	Class4() { 
		func1();
	}
}
