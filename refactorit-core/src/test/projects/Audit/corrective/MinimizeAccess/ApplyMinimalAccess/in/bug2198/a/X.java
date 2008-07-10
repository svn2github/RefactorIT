package bug2198.a;

/**
 * @violations 1
 */

public class X {
  private Inner i = new Inner();
  public class Inner {}
}

class Y extends X {}
