public class Test {
  void method() {
    int var = 0;
    int x = 0;
    /*]*/var = newmethod(x, var);/*[*/
    int k = var;
  }

  int newmethod(final int x, int param) {
    ++param;
    param = x;

    return param;
  }
}
