public class A {
  private int a;

  private class Inner {

    private void f(A ref) {
      A.this.a = 1;
      ref.a = 1;
      A.this.a = 1;
      A.this.a = 1;
    }
  }
}
