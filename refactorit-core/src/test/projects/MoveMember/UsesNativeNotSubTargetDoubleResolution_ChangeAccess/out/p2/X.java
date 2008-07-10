package p2;

import p1.A;


public class X {

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
    a.f9();
  }
}
