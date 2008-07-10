public class A {
  public void method() {
    stayingMethod();
    this.stayingMethod();
    movingMethod();
    this.movingMethod();
  }

  public void stayingMethod() {
  }

  public void movingMethod() {
  }
}

class B {
  {
    A a = new A();
    a.method();
  }  
}
