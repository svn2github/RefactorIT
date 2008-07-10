public class A {
}

class B {

  public void method() {
    method1(this);
  }

  public void method1(B b) {
    b = null;
  }

  public void method2() {
    System.err.println("bug: " + this);
  }
}

class C {
  B b = null;

  static {
    A a = null;
    b.method();
    b.method1(b);
    b.method2();
  }
}
