class Test {
  static {
    B b = new C();
    b.method2();
  }
}

interface A {
  void method();
}

abstract class B implements A {
  public abstract void method();
  
  public void method2() {
    this.method();
  }
}

class C extends B {
  public void method() {
  }
}
