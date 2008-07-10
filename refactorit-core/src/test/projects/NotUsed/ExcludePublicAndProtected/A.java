public class A {
  public int a;
  protected int b;
  int c;
  private int d;

  {
    B b;
  }

  public A() {
  }

  protected A(int a) {
  }

  A(int a, int b) {
  }

  private A(int a, int b, int c) {
  }

  public void f1() {
  }

  protected void f2() {
  }

  void f3() {
  }

  private void f4() {
  }
}

class B {
  {
    A a;
  }
}
