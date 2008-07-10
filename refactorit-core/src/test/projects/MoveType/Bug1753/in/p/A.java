package p;

public class A {

  public static void k() {}

}

class Test {

  public void f() {
    new A().k();
    A.k();
  }

}
