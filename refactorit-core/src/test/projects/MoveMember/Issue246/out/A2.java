public class A2 extends A {

  public void foo(B b) {
      b.foo(this, b);
  }
}