
public class Test {
  private int field = 0;
  private int field2 = 0;
  private int field3 = 0;
  private int field4 = 0;
  private int field5 = 0;
  private int field6 = 0;

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

  public void method() {
    field = 2;
    field2 = 2;
  }
}
