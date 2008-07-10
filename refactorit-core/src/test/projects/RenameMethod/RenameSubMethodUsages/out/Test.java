
public class Test {
  public void method1() {
  }
}

class Test2 extends Test {
  public void method1() {
  }

  {
    new Test2().method1();
  }
}