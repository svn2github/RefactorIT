package CheckPossibleAccessRights12.p1;

/**
 * @violations 1
 */
public class A {
  protected A(boolean isShowSource) {
  }

  public boolean isShowSource() {
    return true;
  }
}
