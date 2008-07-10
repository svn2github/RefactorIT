package bbb;

import aaa.AAA;


public class BBB extends AAA {
  public void method() {
    new Runnable() {
      public void run() {
        new Runnable() {
          public void run() {
            field.toString();
          }
        };
      }
    };
  }
}
