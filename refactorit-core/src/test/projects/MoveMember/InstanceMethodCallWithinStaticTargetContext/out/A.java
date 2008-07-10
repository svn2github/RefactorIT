public class A {
  static A a = null;

  static {
    a.method();
  }

  public static void staticMethod() {
    B b = new B();
    a.method();
  }

  public void nonStaticMethod() {
    B b = new B();
    method();
  }

  public void method() {
  }
}

class B {
}
