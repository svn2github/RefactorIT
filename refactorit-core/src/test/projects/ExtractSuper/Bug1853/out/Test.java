
public class Test extends SuperTest {

  private class Inner {
    private int innerField = field3;

    public Inner() {
      innerField = field4;
    }

    private void method() {
      int local = field5;
    }
  }

  {
    field6 = 1234;
  }

  public Test() {
    field = 1;
    field2 = 1;
  }
}
