package locals_out;

public class A_test553 {

  public void foo() {
    int i = 0;
    for (int x = i++, y = x; true;) {
      /*]*/extracted(i);/*[*/
    }
  }

  protected void extracted(final int i) {
    int x;
    x = i;
  }
}

