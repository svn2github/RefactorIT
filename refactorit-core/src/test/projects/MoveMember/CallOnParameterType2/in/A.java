public class A {
  public void method(B b) {
    method1(b);
  }

  public void method1(B b) {
    b = null;
  }

  public void method2(B b) {
    System.err.println("bug: " + b);
  }
}

class B {
}

class C {
  B b = null;

  static {
    A a = null;
    a.method(b);
    a.method1(b);
    a.method2(b);
  }
}
