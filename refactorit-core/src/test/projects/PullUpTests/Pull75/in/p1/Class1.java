package p1;
import p2.Class10;

public class Class1 {
}

class Class2 extends Class10 {
// change super to this
	public int func1() {
		int b = super.a;
		super.f10();
		super.f10();
		int b = super.f11();
		super.f100();
		return super.f12(super.f13());
	}
}
