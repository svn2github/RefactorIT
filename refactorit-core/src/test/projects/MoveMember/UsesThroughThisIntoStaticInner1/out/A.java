public class A {
  private int field;

  private static class Inner {

    private void f(A a, A ref) {
      a.field = 1;
      ref.field = 1;
      a.field = 1;
      a.field = 1;
    }
  }
}
