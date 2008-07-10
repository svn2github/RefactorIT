public class A {
  private A() { }

  public void f() {
    new A();
  }
}

class B {
}
