package y;

import y.TestException.*;
import y.TestException.InnerException;

class TestException extends Exception {
  public void method() throws y.TestException, y.TestException.InnerException {
    throw new y.TestException();
    throw new y.TestException.InnerException();
    try {} catch (y.TestException e) {} catch(y.TestException.InnerException e1) {}
  }

  static class InnerException extends Exception {
  }
  
}
