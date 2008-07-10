package p1;
import p2.Class10;

public class Class1 {
}

class Class2 extends Class10 {
// check if foreign method is accessible after pull up
	public void func1() {
		new Class0().f();
	}
}
