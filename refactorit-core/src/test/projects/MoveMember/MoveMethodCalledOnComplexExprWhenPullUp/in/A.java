public class A {
  {
    new B().method(); // doesn't need to edit this
  }
}

class B extends A {
  public void method() {
  }
}
  