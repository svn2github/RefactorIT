package TestCatches;

import java.io.*;

/**
 * @violations 13
 */
public class A {	
	public void test() {
		try {
			meth();
		} catch(IOException e) {
		} finally {}
	}
	
	public void meth() throws IOException {
	}
}

class B {	
	public void test() {
		try {
			meth1();
			meth2();
		} catch(IOException e) {
		} finally {}
	}
	
	public void meth1() throws IOException {
	}
	
	public void meth2() throws IOException {
		throw new IOException();
	}
}

class C {
	public void test() {
		try {
			meth1();
			meth2();
		} catch(IOException e) {
		} finally {}
	}
	
	public void meth1() throws FileNotFoundException {
	}
	
	public void meth2() throws InterruptedIOException {
		throw new InterruptedIOException();
	}
}

class D {
  public void test() {
    try {
      meth();
    } catch(IOException e) {
    }
  }
  
  public void meth() throws IOException {
    
  }
}

class E {
  public void test() {
    try {
      meth1();
      meth2();
      meth3();
    } catch(EOFException e) {
      /*
       * EOFException is caught here
       */
    } catch(NullPointerException e) {
      /**
       * NPE is caught here
       */
    } catch(FileNotFoundException e) {
      // /*FileNotFoundException is caught here*/
    } catch(Exception e) {
      // /*
      //  * Just exception, should not be excluded
      //  */
    }
    
  }
  
  public void meth1() throws FileNotFoundException {}
  public void meth2() {}
  public void meth3() throws EOFException {}
}

class F {
  public void test() {
    try {
      meth1();
    } catch(RuntimeException e) {
    }
  }
  
  public void meth1() throws ClassCastException {
  }
}

class G {
  public void test() {
    try/*comment*/{meth1();/*comment*/} 
    catch(FileNotFoundException e) {}
  }
  
  public void meth1() throws FileNotFoundException {
  }
}

class H {
  public void test() {
    try{
      meth1();
      meth2();
      meth3();
      meth4();
      FileReader fr = new FileReader("foo");
      fr.read();
    }
    catch(MyBException e) {}
    catch(FileNotFoundException e) {/*should remain*/}
    catch(EOFException e) {/*should remain*/}
    catch(MyException e) {}
    catch(NullPointerException e) {/*should remain*/}
    catch(IOException e) {/*should remain*/}
    catch(Exception e) {/*should remain*/}
  }
  
  public void meth1() throws FileNotFoundException {}
  public void meth2() throws EOFException {throw new EOFException();}
  public void meth3() throws IOException , MyAException, MyBException {}
  public void meth4() throws NullPointerException, FileNotFoundException {}
  public void meth5() throws Exception {}
  
  class MyException extends Exception {}
  class MyAException extends MyException {}
  class MyBException extends MyException {}
}