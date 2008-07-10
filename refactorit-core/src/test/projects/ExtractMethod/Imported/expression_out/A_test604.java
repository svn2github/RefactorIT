package expression_out;

public class A_test604 {

  public void foo() {
    Object o = null;
    if (/*]*/extracted(o)/*[*/)
      foo();
  }

  protected boolean extracted(final Object o) {
    return o == o;
  }
}
