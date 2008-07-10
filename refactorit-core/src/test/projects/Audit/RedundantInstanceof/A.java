package RedundantInstanceof;

import java.awt.*;
import java.awt.image.*;


public class A {
  /**
   * @audit RedundantInstanceof
   */
  public void test1(){
    if(getWindow() instanceof Object){
    }
  }

  /**
   * @audit RedundantInstanceof
   */
  public void test2(){
    if(getWindow() instanceof ImageObserver){
    }
  }
  
  /**
   * @audit RedundantInstanceof
   */
  public void test3(){
    if(getWindow() instanceof Window){
    }
  }
  
  /**
   */
  private Window getWindow(){
    return (Window)null;
  }
}
