package p2;

import p1.A;
import p1.C;


public class X {

  public void f1(A a, C c) {
    a.f2();
    c.f3();
  }

/***************/

  public static void f4() {
    C.f5();
  }

/***************/

  public void f6() {
    C.f7();
  }

/***************/

  public static void f8(C c) {
    c.f9();
  }
}
