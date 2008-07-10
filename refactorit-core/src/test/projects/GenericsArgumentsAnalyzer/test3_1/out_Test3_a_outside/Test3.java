/*
 * Test3.java
 *
 * Created on March 22, 2005, 2:17 PM
 */

package genericsrefact.test3_1;

import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Test3 {
  
  /** Creates a new instance of Test3 */
  public void a() {
    Blah bar = new Blah();
    List<Number> outside;
    bar.method(outside);
  }
  
}
