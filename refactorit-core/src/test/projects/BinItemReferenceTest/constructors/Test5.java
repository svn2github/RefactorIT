/*
 * Test5.java
 *
 * Created on April 19, 2005, 2:09 PM
 */

package constructors;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Test5 {
  public void test(){
    ArrayList list = new ArrayList(0){
      public Object get(int i){
        return "element["+i+"]";
      }
    };
  }
}
