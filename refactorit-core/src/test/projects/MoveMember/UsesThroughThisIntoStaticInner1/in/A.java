public class A {
  private int field;

  private void f(A ref) {
    field = 1;
    ref.field = 1;
    this.field = 1;
    A.this.field = 1;
  }

  private static class Inner {
  }
}
