package RedundantCast;

import java.awt.*;
import java.awt.image.*;


public class Primitives {
  /**
   * @audit RedundantCast
   */
  void test1(){
    int i = 5;
    boolean trip = false;
    
    int data[] = null;
    int val = (0xFF & (int) data[i]);
  }

  /**
   * @audit RedundantCast
   */
  void test2(){
    byte[] data = new byte[1];
    int val = 0xFF & (int) data[0];
  }

  /**
   *
   */
  void test3(){
    int a = 1;
    int b = 2;
    float c = ((float) a)/b; /* classical case to avoid loss of priscision when dividing two integers in floating point context (no violation!) */
  }
}
