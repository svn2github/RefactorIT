package p1;

public class A {
  void usesInner() {
    A.Inner inner = new Inner();
    inner.foo();
    int b = Inner.BAR;
  }

  public class Inner {
    static int BAR = 0;
    
    public void foo() {
    }
  }
}