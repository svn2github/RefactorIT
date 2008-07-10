package bug2027.p2;

/**
 * @violations 1
 */

public class B extends bug2027.p1.A {

  private B() {
    super();
    doSomething();
  }
}
