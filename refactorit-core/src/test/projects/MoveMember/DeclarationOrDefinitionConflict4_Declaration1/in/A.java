public class A extends B {
  public C ref;

  public void f() {
    ref.a = 1;
  }
}

class C {
  public int a;
}

abstract class B {
}
