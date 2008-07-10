public class A {
  public void f1() {
    f2();
  }

  private void f2() {
  }
}

class B {
  public void f1(A a) { }
}
