package p1;

class B {
  {
    B.method();
  }

  public static void method() {
    method();
    B.method();
    A.anotherMethod();
  }
}

class C {
  static {
    p1.B.method();
  }
}

public class A {

  public static void anotherMethod() {
    B.method();
  }

  public void methodX() {
    B.method(); B.method();
  }
}
