package p;

public class Test {
  A ax = new A();

  public Test() {
    new B(new A[] { ax });
  }
}
