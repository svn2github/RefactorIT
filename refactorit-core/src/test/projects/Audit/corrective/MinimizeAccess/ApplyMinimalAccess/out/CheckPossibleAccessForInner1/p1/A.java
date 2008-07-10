package CheckPossibleAccessForInner1.p1;
/**
 * @violations 3
 */
public class A {
	private void f() {
		Inner1 a;
	}

	private class Inner1 {
	}

	class Inner2 {
	}

	public class Inner3 {
	}
}
