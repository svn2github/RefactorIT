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