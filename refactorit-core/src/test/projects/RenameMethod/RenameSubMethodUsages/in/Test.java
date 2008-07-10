
public class Test {
  public void method() {
  }
}

class Test2 extends Test {
  public void method() {
  }

  {
    new Test2().method();
  }
}