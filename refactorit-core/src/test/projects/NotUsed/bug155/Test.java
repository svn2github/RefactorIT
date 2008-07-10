public class Test {
  static{
    B b = new B();
    b.method1();

    A a; // so that A in general is used
  }
}

class A {
  public void method() {
  }
}

class B extends A {
  public void method() {
  }
  
  public void method1() {
    this.method(); // here it uses B.method() only
  }
}
