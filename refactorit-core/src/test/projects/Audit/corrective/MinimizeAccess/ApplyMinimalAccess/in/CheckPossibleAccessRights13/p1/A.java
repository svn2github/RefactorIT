package CheckPossibleAccessRights13.p1;

/**
 * @violations 6
 */
public class A {
  public int a1;
  public int a2;
  public int a3;
  public int a4;
  public int a5;
  public int a6;
}

class B {
  {
    new A().a2 = 1;
  }
}
