public class A {
  public void f1() {
    A a = new A();
    f2();
    a.f3();
  }

  public void f2() { }

  public void f3() { }

/***************/

  public static void f4() {
    f5();
  }

  public static void f5() { }

/***************/

  public void f6() {
    f7();
  }

  public static void f7() { }

/***************/

  public static void f8() {
    A a = new A();
    a.f9();
  }

  public void f9() { }
}

class B extends A {
}
