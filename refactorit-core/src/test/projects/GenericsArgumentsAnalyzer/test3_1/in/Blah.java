/*
 * Blah.java
 *
 * Created on March 22, 2005, 2:17 PM
 */

package genericsrefact.test3_1;

import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Blah extends Foo {
  
  /** Creates a new instance of Blah */
  public Blah() {
  }
  
  public void method(List blahParam){
    List blahVar = blahParam;
    blahVar.add(5.5);
  }
  
}
