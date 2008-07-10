/*
 * test2.java
 *
 * Created on April 7, 2005, 1:29 PM
 */

package genericsrefact.test2;

import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Test2 {
  
  public void a() {
    List<A> list;
    list.add(new A());
    list.add(new B());
  }
  
}

class A {
  
}

class B extends A {
  
}
