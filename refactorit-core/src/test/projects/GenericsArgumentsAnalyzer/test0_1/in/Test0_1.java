/*
 * Test0_1.java
 *
 * Created on April 13, 2005, 1:01 PM
 */

package genericsrefact.test0_1;

import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Test0_1 {
  
  /** Creates a new instance of Test0_1 */
  public Test0_1() {
  }
  
  public void test0(){
    B b = new B();
    A a = b.getA("lalala");
  }
  
  public void test1(){
    C c = new C();
    A a = c.getB().getA("lalala");
  }
}

class A<TA> {
  
}

class B<TB> {
  A<List<TB>> getA(TB obj){
    return null;
  }
}

class C<TC> {
  B<TC> getB(){
    return null;
  }
}
