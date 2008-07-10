public class A {
  public void method() {
  }
}

class B {
  {
    new A().method();
  }
}

class C {
}
