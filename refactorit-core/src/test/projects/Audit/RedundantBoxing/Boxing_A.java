/*
 * Boxing_A.java
 *
 * Created on November 17, 2004, 4:21 PM
 */

package RedundantBoxing;

public class Boxing_A {
  
  public void takesIntegerObj(Integer aaa){
    
  }
  
  public void takesWhateverInteger(Integer aaa){
    
  }
  
  public void takesWhateverInteger(int aaa){
    
  }
  
  /**
   * @audit RedundantBoxing
   */
  public void a(){
    Integer a = new Integer(5);
  }
  
  /**
   * @audit RedundantBoxing
   */
  public void b(){
    Number b = new Integer(5);
  }
  
  /**
   *
   */
  public void c(){
    Double c = new Float(5);
  }
  
  /**
   *
   */
  public void d(){
    Double c = new Double("5.5");
  }
  
  /**
   *
   */
  public void e(){
    int a = 5;
    Double c = new Double(a);
  }
  
  /**
   * @audit RedundantBoxing
   */
  public void f(){
    takesIntegerObj(new Integer(5));
  }
  
  /**
   * @audit ProbablyRedundantBoxing
   */
  public void g(){
    takesWhateverInteger(new Integer(5));
  }
}
