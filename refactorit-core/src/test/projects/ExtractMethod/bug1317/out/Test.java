
public class Test {

  private void test() {
    int i = 1;
    int j;
    /*]*/
    newmethod(i);
    /*[*/
  }

  void newmethod(final int i) {
    int j;
    j = i;
  }
}
