package return_out;

public class A_test700 {

  public void foo() {
    /*]*/extracted();
    return;/*[*/
  }

  protected void extracted() {
    return;
  }
}
