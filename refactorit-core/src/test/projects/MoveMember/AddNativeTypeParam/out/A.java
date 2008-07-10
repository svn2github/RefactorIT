public class A {

  public void stayingMethod() {
  }
}

class B {
  {
    A a = new A();
    method(a);
  }  

  public void method(A a) {
    a.stayingMethod();
    a.stayingMethod();
    movingMethod();
    movingMethod();
  }

  public void movingMethod() {
  }
}
