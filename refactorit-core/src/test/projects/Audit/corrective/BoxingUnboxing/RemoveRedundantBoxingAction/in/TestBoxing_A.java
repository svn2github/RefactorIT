/*
 * Boxing_A.java
 *
 * Created on November 17, 2004, 4:21 PM
 */

package corrective.BoxingUnboxing.RemoveRedundantBoxingAction.in;

/**
 * @violations 4
 */
public class TestBoxing_A {
  
  public void takesIntegerObj(Integer aaa){
    
  }
  
  public void takesWhateverInteger(Integer aaa){
    
  }
  
  public void takesWhateverInteger(int aaa){
    
  }
  
  public void a(){
    Integer a = new Integer(5);
  }
  
  public void b(){
    Number b = new Integer(5);
  }
  
  public void c(){
    Double c = new Float(5);
  }
  
  public void d(){
    Double c = new Double("5.5");
  }
  
  public void e(){
    int a = 5;
    Double c = new Double(a);
  }
  
  public void f(){
    takesIntegerObj(new Integer(5));
  }
  
  public void g(){
    takesWhateverInteger(new Integer(5));
  }
}
