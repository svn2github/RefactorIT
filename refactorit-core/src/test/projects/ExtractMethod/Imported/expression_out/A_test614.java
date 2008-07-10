package expression_out;

import java.io.File;
import A;


public class A_test614 {

  public void foo() {
    A a = null;
    a.useFile(/*]*/extracted(a)/*[*/);
  }

  protected File extracted(final A a) {
    return a.getFile();
  }
}
