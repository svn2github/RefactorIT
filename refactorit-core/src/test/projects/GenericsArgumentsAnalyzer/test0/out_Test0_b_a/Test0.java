/*
 * Test0.java
 *
 * Created on April 13, 2005, 12:30 PM
 */

package genericsrefact.test0;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Test0 {
  
  public void a(){
    A a = new A();
    String b = (String) a.genericField;
  }
  
  public void b(){
    A<String> a = new A<String>();
    a.genericField = "lalala";
  }
  
  public void c(){
    A a = new A("lalala");
  }
}

class A<T> {
  public T genericField;
  
  public A(){
    
  }
  
  public A(T genericConstructorParam){
    genericField = genericConstructorParam;
  }
}
