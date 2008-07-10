package p1;

public class Class1 {
}

class Class2 {
}

class Class3 extends Class2 {
// push down when sub of new type also invokes
	public void func1() { }
}

class Class4 extends Class3 {
	Class4() { 
		func1();
	}
}
