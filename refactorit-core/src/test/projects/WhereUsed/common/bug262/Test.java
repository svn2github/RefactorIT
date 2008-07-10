interface SomeInterface {
  void a();
}

abstract class A implements SomeInterface {
  public void a() {}
}

class B extends A {
}

class Test {
  void test() {
    SomeInterface i = new B();
    i.a();
    
    A a = new B();
    a.a();
  }
}
