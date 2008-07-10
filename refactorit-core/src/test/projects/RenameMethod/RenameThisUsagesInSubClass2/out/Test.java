
public abstract class Test {
  public abstract void anotherMethod1();
}

class Test2 extends Test {
  public void anotherMethod1() {
  }

  {
    anotherMethod1();
  }
}
