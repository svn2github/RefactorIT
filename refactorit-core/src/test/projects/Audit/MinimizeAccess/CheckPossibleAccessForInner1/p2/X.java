package CheckPossibleAccessForInner1.p2;

import CheckPossibleAccessForInner1.p1.A;

public class X {
  /**
   * @audit MinimizeAccessViolation
   */
	public A.Inner3 f() {
		return null;
	}
}
