
public class Test {

  public Test() throws Exception {
    throw new Exception();
  }

  private void method() throws Exception {
    /*]*/newmethod();/*[*/
  }

  void newmethod() throws Exception {
    new Test();
  }

}
