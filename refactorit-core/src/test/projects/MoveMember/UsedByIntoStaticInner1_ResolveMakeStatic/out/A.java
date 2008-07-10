public class A {
  Inner in = new Inner();

  private static void f1(A ref) {
    Inner.f();
  }

  private static class Inner {

    private static void f() {
    }
  }
}
