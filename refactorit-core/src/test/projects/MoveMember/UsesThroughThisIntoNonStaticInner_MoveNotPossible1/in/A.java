public class A {
	Inner in = new Inner();

	public static void main(String[] args) {
		A ref = new A();
		ref.f();
	}

	public void f() { }

	public class Inner {
	}
}
