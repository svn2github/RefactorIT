package CheckPossibleAccessForInner1.p1;

public class A {
  /**
   * @audit MinimizeAccessViolation
   */
	public void f() {
		Inner1 a;
	}
  /**
   * @audit MinimizeAccessViolation
   */
	public class Inner1 {
	}
  /**
   * @audit MinimizeAccessViolation
   */
	public class Inner2 {
	}

	public class Inner3 {
	}
}
