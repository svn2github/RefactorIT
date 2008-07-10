package p;

public class B {
  private I[][] classes = new A[0][0];

  public B() {
    classes[0][0] = buildA();
  }

  public I buildA() {
    return new A();
  }

  public void test() {
  }
}