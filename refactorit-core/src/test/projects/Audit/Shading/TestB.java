/*
 * TestB.java
 *
 * Created on May 3, 2005, 12:50 PM
 */

/**
 *
 * @author  Arseni Grigorjev
 */
public class TestB {
  private int abc = 5;
  
  /**
   * @audit Shading
   */
  public void method(){
    int abc = 6;
  }
  
  /**
   * @audit Shading
   */
  public void method(int abc){
    
  }
  
}
