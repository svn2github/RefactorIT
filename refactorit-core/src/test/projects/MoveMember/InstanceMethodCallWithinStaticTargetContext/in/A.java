public class A {
  static A a = null;

  static {
    new B().method();
  }

  public static void staticMethod() {
    B b = new B();
    b.method();
  }

  public void nonStaticMethod() {
    B b = new B();
    b.method();
  }
}

class B {
  public void method() {
  }
}
