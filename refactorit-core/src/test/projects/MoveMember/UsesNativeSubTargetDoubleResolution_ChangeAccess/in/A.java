public class A {
  public void f1() {
    A a = new A();
    f2();
    a.f3();
  }

  private void f2() { }

  private void f3() { }

/***************/

  public static void f4() {
    f5();
  }

  private static void f5() { }

/***************/

  public void f6() {
    f7();
  }

  private static void f7() { }

/***************/

  public static void f8() {
    A a = new A();
    a.f9();
  }

  private void f9() { }
}

class B extends A {
}
