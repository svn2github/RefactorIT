package issue596;

public class A {
  /**
   * @audit MinimizeAccessViolation
   */
  public void foo() {
  }
  /**
   * @audit MinimizeAccessViolation
   */
  public void bar() {
  }
  
  /**
   * @audit MinimizeAccessViolation
   */
  class Inner {
    void usesFoo() {
      foo();
    }
  }
}