package p;

public class A {
  private int g = 0;

  public A(String str) {
  }

  public void method() {
    new A("") {
      public void method() {
      }
    };
  }
}
