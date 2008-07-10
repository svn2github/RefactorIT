package p;
public class A { }

interface I { }
class B implements I { }
class C implements I { }

class D {
	public void meth(B b) {
	}
	public void meth(C c) {
	}
}

class E extends D {
	public void meth(C c) {
	}
}