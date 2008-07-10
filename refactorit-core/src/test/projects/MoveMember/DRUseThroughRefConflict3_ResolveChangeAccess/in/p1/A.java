package p1;

public class A {
// Move into X
// change access of B.fB() since it is weak for X
  public void f1() {
    new B().fB();
  }
}
