package p1;

public class A {
  private int a;
  public void f1() {
    a = 1;
  }

  public void f2() {
  }

  public void f3() {
    f1();
  }
}
