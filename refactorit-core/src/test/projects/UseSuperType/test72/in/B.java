package p;

public class B {
  private A[][] classes = new A[0][0];

  public B() {
    classes[0][0] = buildA();
  }

  public A buildA() {
    return new A();
  }

  public void test() {
  }
}