package CheckPossibleAccessForInner1.p2;

import CheckPossibleAccessForInner1.p1.A;

/**
 * @violations 1
 */
public class X {
	private A.Inner3 f() {
		return null;
	}
}
