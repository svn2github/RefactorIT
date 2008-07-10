/*
 * Helping.java
 *
 * Created on March 15, 2005, 10:42 AM
 */

package genericsrefact.test6;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Helping {
  
  /** Creates a new instance of Helping */
  public Helping() {
  }
  
  public void takeIterator(Iterator it_p){
    String out = (String) it_p.next();
  }
  
  public void takeList(List lst){
    lst.add("afafd");
  }
  
}
