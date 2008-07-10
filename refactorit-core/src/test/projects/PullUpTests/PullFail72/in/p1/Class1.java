package p1;
import p2.Interface10;

public class Class1 {
}

class Class2 implements Interface10 {
	public Class3 a = Class3.f();
}

class Class3 {
	final static Class3 f() {
		return new Class3();
	}
}
