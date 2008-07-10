package CheckPossibleAccessRights13.p1;

public class A {
  /**
   * @audit MinimizeAccessViolation
   */
  public int a1;
  /**
   * @audit MinimizeAccessViolation
   */
  public int a2;
  /**
   * @audit MinimizeAccessViolation
   */
  public int a3;
  /**
   * @audit MinimizeAccessViolation
   */
  public int a4;
  /**
   * @audit MinimizeAccessViolation
   */
  public int a5;
  /**
   * @audit MinimizeAccessViolation
   */
  public int a6;
}

class B {
  {
    new A().a2 = 1;
  }
}
