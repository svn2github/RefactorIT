package p2;

import p1.A;


public class X {
  private void f2() { }

  private void f3() { }

  private static void f5() { }

  private static void f7() { }

  private void f9() { }

  public void f1(A a1) {
    A a = new A();
    a1.f2();
    a.f3();
  }

/***************/

  public static void f4() {
    A.f5();
  }

/***************/

  public void f6() {
    A.f7();
  }

/***************/

  public static void f8() {
    A a = new A();
    p2.X x = new p2.X();
    a.f9();
  }
}
