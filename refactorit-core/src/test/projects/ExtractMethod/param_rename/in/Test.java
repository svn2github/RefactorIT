
public class Test {

  public void setLength(int i) {
  }

  public void setMethod(String name) {
  }

  public void setRecipient(String name) {
  }

  public void doSome() {
    Test item = new Test();
    /*]*/item.setLength(2);item.setLength(3);
    item.setMethod("up");
    item.setRecipient("juku");/*[*/
    doSomeElse(item);
  }

  public void doSomeElse(Test test) {
  }

}
