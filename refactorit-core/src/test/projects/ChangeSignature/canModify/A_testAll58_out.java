package p;

public abstract class A {
	public abstract int m(String[][][] bbb, int abb);
}
class B extends A {
	public int m(String[][][] bbb, int abb) {
		return abb + 0;
	}
}
class C extends B {
	public int m(String[][][] bbb, int abb) {
		return abb + 17;
	}
}
