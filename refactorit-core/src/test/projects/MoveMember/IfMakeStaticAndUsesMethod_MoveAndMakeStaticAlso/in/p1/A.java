package p1;

public class A {
  public void f1() {
    f2();
  }

  private void f2() {
  }

  public void f3() {
    f1();
  }
}
