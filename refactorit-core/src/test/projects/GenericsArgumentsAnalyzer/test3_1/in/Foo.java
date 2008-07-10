/*
 * Foo.java
 *
 * Created on March 22, 2005, 2:17 PM
 */

package genericsrefact.test3_1;

import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Foo extends Bar {
  
  /** Creates a new instance of Foo */
  public Foo() {
  }
  
  public void method(List fooParam){
    List fooVar = fooParam;
    fooVar.add(5L);
  }
  
}
