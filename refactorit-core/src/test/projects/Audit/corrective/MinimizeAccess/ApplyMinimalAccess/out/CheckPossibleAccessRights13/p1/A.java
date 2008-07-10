package CheckPossibleAccessRights13.p1;

/**
 * @violations 6
 */
public class A {
  private int a1;
  int a2;
  protected int a3;
  protected int a4;
  private int a5;
  private int a6;
}

class B {
  {
    new A().a2 = 1;
  }
}
