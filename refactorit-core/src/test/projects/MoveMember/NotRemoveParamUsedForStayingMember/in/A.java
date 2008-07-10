public class A extends B {
  public void method(A a) {
    a.stayingMethodA();
    a.method(a);
  }

  public void stayingMethodA() {
  }
}

class B {
}
