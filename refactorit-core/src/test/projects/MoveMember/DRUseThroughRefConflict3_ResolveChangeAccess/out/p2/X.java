package p2;

import p1.B;


public class X {

// Move into X
// change access of B.fB() since it is weak for X
  public void f1() {
    new B().fB();
  }
}
