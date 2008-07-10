/*
 * Bar.java
 *
 * Created on March 22, 2005, 2:17 PM
 */

package genericsrefact.test3_1;

import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Bar {
  
  /** Creates a new instance of Bar */
  public Bar() {
  }
  
  public void method(List barParam){
    List barVar = barParam;
    barVar.add(5);
  }
  
}
