
public class Test {

  public void setLength(int i) {
  }

  public void setMethod(String name) {
  }

  public void setRecipient(String name) {
  }

  public void doSome() {
    Test item = new Test();
    /*]*/newmethod(item);/*[*/
    doSomeElse(item);
  }

  void newmethod(final Test testItem) {
    testItem.setLength(2);testItem.setLength(3);
    testItem.setMethod("up");
    testItem.setRecipient("juku");
  }

  public void doSomeElse(Test test) {
  }

}
