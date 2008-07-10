package expression_out;

import java.io.File;
import A;


public class A_test616 {

  public void foo() {
    A a = null;
    /*]*/extracted(a)/*[*/.getName();
  }

  protected File extracted(final A a) {
    return a.getFile();
  }
}
