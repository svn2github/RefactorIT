package p1;

class X {
  public void anotherMethod() {
  }
}

public class A extends X {
  public void method() {
    this.anotherMethod();
    super.anotherMethod();
  }
}
