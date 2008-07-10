package RedundantCast;

import java.awt.*;
import java.awt.image.*;


public class A {
  /**
   * @audit RedundantCast
   */
  void test1(){
    Component cmp = (Component)getWindow();
  }

  /**
   * @audit RedundantCast
   */
  void test2(){
    ImageObserver obs = (ImageObserver)getWindow();
  }

  /**
   * @audit RedundantCast
   */
  void test3(){
    Window wnd = (Window)getWindow();
  }

  /**
   * @audit RedundantCast
   */
  private Window getWindow(){
    return (Window)null;
  }
}
