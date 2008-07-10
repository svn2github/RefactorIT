package LoopCondModification;

import java.io.*;

public class A {
  private int z = 0;

  /**
   * @audit LoopConditionalsModification
   * @audit LoopConditionalsModification
   * 
   */
  public void meth1(int k) {
    for (int i = 0; (i < 10); i = i + 1) {
      for (int k = 0; k < 10; i++) { // bug
      }
      i++; // bug
    }
  }

  /**
   * @audit LoopConditionalsModification
   * 
   */
  public void meth2() {
    for (int i = 0; (i < 10); i = i + 1) {
      i++; // suspicious
    }
  }

  /**
   * @audit LoopConditionalsModification
   * 
   */
  public void meth3() {
    int k = 0;
    while (k < 12) {
      if (k == 3) { // ...
      } else { // ...
      }
      for (k = 0; k < 12; k++) { // bug
      }
    }
  }

  /**
   * @audit LoopConditionalsModification
   * 
   */
  public void meth4() {
    while (z == 0) {
      for (int i = 0; i < 5; z++) { // bug
      }
    }
  }

  /**
   * @audit LoopConditionalsModification
   * 
   */
  public void meth5() {
    for (int i = 0; i < 10; i++) {
      while (i < 10) {
        if (i > 3) {
          i++; // suspicious
        }
      }
    }
  }

  // No violations,

  /**
   * 
   */
  public void meth6() {
    int z = 100;
    String s = "test";
    for (int i = 0; i < z && !"demo".equals(s); i++) {
      for (int k = 0; k < i; k++) {
        s = Integer.toString(i);
      }
    }
  }

  /**
   * 
   */
  public void meth7() {
    int k = 10;
    while (k < 100) {
      k++;
    }
  }

  /**
   * 
   */
  public void meth8() {
    for (int i = 0; i < 10; i++) {
      for (int i = 0; i < 10; i++) {

      }
    }
  }

}
