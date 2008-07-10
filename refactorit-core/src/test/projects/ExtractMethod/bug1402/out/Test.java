
public class Test {

  public void method() {
    int y = 0;

    if (true) {
      int x = y;
    } else {
      /*]*/newmethod(y);/*[*/
    }
  }

  void newmethod(final int y) {
    int z = y;
  }

}
