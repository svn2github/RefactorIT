public class A {
  public void method() {
    otherMethod();
  }

  public void otherMethod() {
  }
}

class B {
  {
    A a = new A();
    a.method();
  }
}

class C {
}
