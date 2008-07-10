public class A {
  Inner in = new Inner();

  private static void f1(A ref) {
    ref.f();
  }

  private void f() {
  }

  private static class Inner {
  }
}
