public class A {
  private int a;

  private void f1(A ref) {
    Inner in = new Inner();
    f();
    ref.f();
    this.f();
    A.this.f();
  }

  private void f() {
  }

  private class Inner {
  }
}
