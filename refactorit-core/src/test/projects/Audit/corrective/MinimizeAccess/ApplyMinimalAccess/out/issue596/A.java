package issue596;

/**
 * @violations 3
 */
public class A {
  private void foo() {
  }
  void bar() {
  }
  
  private class Inner {
    void usesFoo() {
      foo();
    }
  }
}