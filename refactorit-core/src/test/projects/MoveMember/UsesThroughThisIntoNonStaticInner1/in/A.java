public class A {
  private int a;

  private void f(A ref) {
    a = 1;
    ref.a = 1;
    this.a = 1;
    A.this.a = 1;
  }

  private class Inner {
  }
}
