package bug2027.p2;


public class B extends bug2027.p1.A {
  /**
   * @audit MinimizeAccessViolation
   **/
  public B() {
    super();
    doSomething();
  }
}
