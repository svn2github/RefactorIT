package CheckPossibleAccessRights12.p2;

import CheckPossibleAccessRights12.p1.A;

/**
 * @violations 1
 */
public class B extends A {
  private B(A a) {
    super(a.isShowSource());
  }
}
