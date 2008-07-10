package issue596;

/**
 * @violations 3
 */
public class A {
  public void foo() {
  }
  public void bar() {
  }
  
  class Inner {
    void usesFoo() {
      foo();
    }
  }
}