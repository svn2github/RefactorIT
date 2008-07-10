
public abstract class Test {
  public abstract void anotherMethod();
}

class Test2 extends Test {
  public void anotherMethod() {
  }

  {
    anotherMethod();
  }
}
