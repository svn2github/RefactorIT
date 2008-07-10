package p1;

class B {
  {
    A.method();
  }
}

class C {
  static {
    p1.A.method();
  }
}

public class A {
  public static void method() {
    method();
    A.method();
    anotherMethod();
  }

  public static void anotherMethod() {
    method();
  }

  public void methodX() {
    method(); A.method();
  }
}
