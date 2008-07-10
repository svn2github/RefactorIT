
public class B {

	public static class StaticClass {
		public void f10(A param) {
			B.f(param, this);
		}
	}

	public static void f(A a1, StaticClass a) {
		a1.a = a;
	}
}
