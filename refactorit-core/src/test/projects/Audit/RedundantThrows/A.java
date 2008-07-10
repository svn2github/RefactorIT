import java.io.*;

/**
 * OutOfMemoryError inherits from Error
 * IllegalArgumentExceptions inherits from RuntimeException
 */
public class A extends B {
  /**
   */
  public void test1() throws Exception {
    throw new Exception();
  }
  
  /**
   * @audit RedundantThrows Exception
   */
  public void test2() throws Exception {
  }
  
  /**
   * @audit RedundantThrows IOException
   */
  public void test3() throws Exception, IOException {
    throw new Exception();
  }
  
  /**
   * @audit RedundantThrows Exception
   */
  public void test4() throws Exception, IOException {
    throw new IOException();
  }
  
  /**
   * @audit RedundantThrows Exception
   * @audit RedundantThrows IOException
   */
  public void test5() throws Exception, IOException {
  }
  
  /**
   * @audit RedundantThrows IOException
   * @audit RedundantThrows FileNotFoundException
   */
  public void test6() throws Exception, IOException, FileNotFoundException {
    throw new Exception();
  }
  
  /**
   * @audit RedundantThrows Exception
   * @audit RedundantThrows FileNotFoundException
   */
  public void test11() throws Exception, IOException, FileNotFoundException {
    throw new IOException();
  }
  
  /**
   * 
   */
  public void test7() throws OutOfMemoryError {
  }
  
  /**
   * 
   */
  public void test8() throws Throwable, OutOfMemoryError {
    throw new Throwable();
  }
  
  /**
   * 
   */
  public void test9() throws IllegalArgumentException {
  }

  /**
   * @audit RedundantThrows Exception
   */
  public void test10() throws Exception, IllegalArgumentException {
    throw new IllegalArgumentException();
  }
  
  
  /**
   * 
   */
  public void test12() throws Exception, IllegalArgumentException {
  }
}

abstract class B {
  abstract void test12() throws Exception;
}

class C extends B {
  public void test12() throws Exception {
    throw new Exception();
  }
}

class AException extends Exception {}
class BException extends AException {}
class CException extends BException {}


class D {
  void meth1() throws Exception {
    throw new BException();
  } 
  
  /**
   * @audit RedundantThrows Exception
   * @audit RedundantThrows BException
   */
  void meth2() throws Exception, AException, BException{
    throw new AException();
  } 
 
  /**
   * @audit RedundantThrows Exception
   * @audit RedundantThrows AException
   */
  void meth3() throws Exception, BException, AException {
    if(1 < 3) {
      throw new BException();
    } else {
      throw new CException();
    }
  }
  
}
