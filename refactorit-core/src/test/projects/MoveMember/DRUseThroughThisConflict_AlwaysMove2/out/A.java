public class A {
}

class B {
  public void f1(A a) { }

  public void f1() {
    f2();
  }

  private void f2() {
  }
}
