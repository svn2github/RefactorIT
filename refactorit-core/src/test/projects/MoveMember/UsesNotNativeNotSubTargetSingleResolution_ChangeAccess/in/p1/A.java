package p1;

class B {
  protected void f2() { }
}

public class A extends B {
  public void f1(C c) {
    f2();
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
