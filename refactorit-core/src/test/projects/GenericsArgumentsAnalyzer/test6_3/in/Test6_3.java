/*
 * Test6_1.java
 *
 * Created on March 29, 2005, 3:48 PM
 */

package genericsrefact.test6_3;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Test6_3<T> {
  
  /** Creates a new instance of Test6_1 */
  public void a() {
    java.util.Vector list;
    Iterator it = list.iterator();
    String a = (String) it.next();
  }
  
}
