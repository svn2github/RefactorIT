package TestHierarchies;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @violations 15
 */
public class J implements I1{
  // IOException throw is used in hierarchy in LB.meth1()
  public void meth1() throws IOException {
  }

  //IOException throw is used in hierarchy in LB.meth1()
  public void meth2() throws IOException {
  }
  
  public void meth3() throws FileNotFoundException {
  }
  
  public void meth4() throws FileNotFoundException {
  }
  
  public void meth5() throws FileNotFoundException {
  }
  
  // IOException throw is used in M.meth6()
  public void meth6() throws IOException {
  }
  
  public void meth7() throws FileNotFoundException {
  }
}

class K extends J implements I3 {
  public void meth1() throws IOException {
  }

  public void meth2() throws IOException {
  }
  
  public void meth3() throws FileNotFoundException {
  }
  
  public void meth4() throws FileNotFoundException {
  }
  
  public void meth5() throws FileNotFoundException {
  }
}

class LA extends K {
  public void meth1() throws IOException {
  }
  
  public void meth2() throws IOException {
  }
  
  public void meth3() throws FileNotFoundException {
  }
  
  public void meth4() throws FileNotFoundException {
  }
  
  public void meth5() throws FileNotFoundException {
  }
}

class LB extends K implements I2{
  public void meth1() throws IOException {
    throw new IOException();
  }
  
  public void meth2() throws IOException {
    throw new FileNotFoundException();
  }
  
  public void meth3() throws FileNotFoundException {
  }
  
  public void meth4() throws FileNotFoundException {
  }
  
  public void meth5() throws FileNotFoundException {
  }
}

class M implements I3, I4{
  public void meth6() throws IOException {
    throw new IOException();
  }
  
  public void meth7() throws FileNotFoundException {
  }
}

class N implements I4 {
  public void meth7() throws FileNotFoundException {
    throw new FileNotFoundException();
  }
}

interface I1 {
  public void meth4() throws FileNotFoundException;
}

interface I2 {
  public void meth5() throws FileNotFoundException;
}

interface I3 {
  public void meth6() throws IOException; 
}

interface I4 {
  public void meth7() throws FileNotFoundException;
}