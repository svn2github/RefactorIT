package TestClauses;

import java.io.*;

/**
 * @violations 16
 */
public class A {
  public void meth1() throws Exception {
    throw new BException();
  } 
  
  /**
   * @audit RedundantThrows Exception
   * @audit RedundantThrows BException
   */
  public void meth2() throws Exception, AException, BException {
    throw new AException();
  } 
 
  /**
   * @audit RedundantThrows Exception
   * @audit RedundantThrows AException
   */
  public void meth3() throws Exception, BException, AException {
    if(1 < 3) {
      throw new BException();
    } else {
      throw new CException();
    }
  }
  
  /**
   * @audit RedundantThrows Exception
   * @audit RedundantThrows BException
   * @audit RedundantThrows CException
   * @audit RedundantThrows DException
   * @audit RedundantThrows IOException
   * @audit RedundantThrows CharConversionException
   * @audit RedundantThrows EOFException
   */
  public void meth4(int code) throws Exception, AException, BException, CException, 
     DException, IOException, CharConversionException, EOFException {
    switch(code) {
      case 0: {
        throw new AException();
      }

      case 1: {
        throw new BException();
      }

      case 2: {
        throw new CException();
      }

      case 3: {
        throw new DException();
      }

      case 4: {
        throw new IOException();
      }

      case 5: {
        throw new CharConversionException();
      }

      case 6: {
        throw new EOFException();
      }
    }
  }
  
  public void meth5(int code) /*next follows throws clause*/throws/*comment*/ 
  Exception, AException, BException, CException, //comment
  /**
   * Sophisticated javadoc
   */
  DException, IOException, CharConversionException, EOFException/*comment*/ {
    switch(code) {
      case 0: {
        throw new CException();
      }

      case 1: {
        throw new EOFException();
      }
    }
  }
}

class AException extends Exception {}
class BException extends AException {}
class CException extends BException {}
class DException extends CException {}