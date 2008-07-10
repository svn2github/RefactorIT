package checkExtends2;

public class A {
  /**
   * @audit MinimizeAccessViolation
   */
  public A() {
  }
}

class B extends A {
  /**
   * @audit MinimizeAccessViolation
   */
 public B() {
 }
}
