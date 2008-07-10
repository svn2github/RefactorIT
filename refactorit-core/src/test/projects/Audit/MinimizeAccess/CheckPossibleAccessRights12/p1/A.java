package CheckPossibleAccessRights12.p1;

public class A {
  /**
   * @audit MinimizeAccessViolation
   */
  public A(boolean isShowSource) {
  }

  public boolean isShowSource() {
    return true;
  }
}
