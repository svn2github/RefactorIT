package p2;

class Class100 {
	public void f100() { }
}

public class Class10 extends Class100 {
	public int a = 1;

	public void f10() { }

	public int f11() {
		return 1;
	}

	public int f12(int i) {
		return i;
	}

	public int f13() {
		return 2;
	}
// change super to this
	public int func1() {
		int b = this.a;
		this.f10();
		this.f10();
		int b = this.f11();
		super.f100();
		return this.f12(this.f13());
	}
}
