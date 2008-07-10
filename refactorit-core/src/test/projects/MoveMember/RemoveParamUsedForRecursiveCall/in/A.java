public class A {
  public void method(B b) {
    b.method(b);
  }
}

class B extends A {
}
