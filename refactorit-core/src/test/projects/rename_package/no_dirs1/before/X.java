package x;

import x.TestException.*;
import x.TestException.InnerException;

class TestException extends Exception {
  public void method() throws x.TestException, x.TestException.InnerException {
    throw new x.TestException();
    throw new x.TestException.InnerException();
    try {} catch (x.TestException e) {} catch(x.TestException.InnerException e1) {}
  }

  static class InnerException extends Exception {
  }
  
}
