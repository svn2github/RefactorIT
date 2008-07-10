package p1;
import p2.Class10;

public class Class1 {
}

class Class2 extends Class10 {
// import not possible
	public void func1() {
		new Class3();
	}
}

class Class3 {
	Class3() { }
}
