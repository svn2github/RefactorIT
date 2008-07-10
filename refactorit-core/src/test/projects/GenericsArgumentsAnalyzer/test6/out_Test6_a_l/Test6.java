package genericsrefact.test6;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Test6<T6> {
  
  /** Creates a new instance of Test6 */
  public Test6() {
  }
  
  public void a(){
    Helping helping = new Helping();
    
    List<T6> list = new ArrayList<T6>();
    
    Iterator it_v = list.iterator();
    List<T6> l = list.subList(0, 1);

    helping.takeIterator(it_v);
    helping.takeList(l);
  }
  
}
