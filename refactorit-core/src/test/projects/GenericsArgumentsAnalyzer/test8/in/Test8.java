/*
 * Test8.java
 *
 * Created on March 16, 2005, 2:23 PM
 */

package genericsrefact.test8;

import java.util.Map;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Test8 {
  
  /** Creates a new instance of Test8 */
  public void test8() {
    B8 b8 = new B8();
    b8.put("lalala", 5, 5.5);
    Map map = b8.getA8().getMap();
  }
  
}
