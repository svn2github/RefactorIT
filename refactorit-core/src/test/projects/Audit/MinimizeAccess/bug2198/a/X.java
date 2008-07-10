package bug2198.a;

public class X {
  private Inner i = new Inner();
  /**
   * @audit MinimizeAccessViolation
   */
  public class Inner {}
}

class Y extends X {}
