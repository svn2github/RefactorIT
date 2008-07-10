package CheckPossibleAccessRights12.p2;

import CheckPossibleAccessRights12.p1.A;

public class B extends A {
  /**
   * @audit MinimizeAccessViolation
   */
  public B(A a) {
    super(a.isShowSource());
  }
}
