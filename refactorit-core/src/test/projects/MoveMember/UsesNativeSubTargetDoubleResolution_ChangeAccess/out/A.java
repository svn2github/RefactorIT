public class A {

  void f2() { }

  void f3() { }

  static void f5() { }

  static void f7() { }

  void f9() { }
}

class B extends A {

  public void f1() {
    A a = new A();
    f2();
    a.f3();
  }

/***************/

  public static void f4() {
    f5();
  }

/***************/

  public void f6() {
    f7();
  }

/***************/

  public static void f8() {
    A a = new A();
    a.f9();
  }
}
