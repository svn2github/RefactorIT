package p1;
import p2.Class10;

public class Class1 {
}

class Class2 extends Class10 {
}

class Class3 {
	public void f() {
		new Class2().func1();
	}
}
