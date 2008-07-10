package p1;

public class Class1 {
}

class Class2 {
	public void func0() { }
// checking bugfix: wrong results when checking during push down if new type overrides moved member
	public void func1() { }
}

class Class3 extends Class2 {
	public void func0() { }
}

class Class4 extends Class2 {
	Class4() { 
		func1();
	}
}
