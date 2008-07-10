public class A extends B {

  public void stayingMethodA() {
  }
}

class B {

  public void method(A a) {
    a.stayingMethodA();
    a.method(a);
  }
}
