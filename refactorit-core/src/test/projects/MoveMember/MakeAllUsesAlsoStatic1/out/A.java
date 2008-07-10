public class A {
}

class B {
  {
    A a = new A();
    C.method();
  }
}

class C {

  public static void method() {
    otherMethod();
  }

  public static void otherMethod() {
  }
}
