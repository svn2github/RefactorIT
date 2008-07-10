package locals_out;

public class A_test506 {

  public void foo() {
    int x = 0;

    /*]*/extracted(x);/*[*/
  }

  protected void extracted(final int x) {
    bar(x);
  }

  public void bar(int i) {
  }
}
