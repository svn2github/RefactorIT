public class A {
  private int a;

  private void f1(A ref) {
    Inner in = new Inner();
    in.f();
    in.f();
    in.f();
    in.f();
  }

  private class Inner {

    private void f() {
    }
  }
}
