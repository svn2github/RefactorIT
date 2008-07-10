package p1;
import p2.X;

public class A extends X {

}

class B {
	private void f() {
		A refA = new A();
		refA.f1();
	}
}
