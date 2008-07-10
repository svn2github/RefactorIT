package CheckExplicitConstructorCall.p2;

import CheckExplicitConstructorCall.p1.A;

public class B extends CheckExplicitConstructorCall.p1.A {
  /**
   * @audit MinimizeAccessViolation
   */
 public B() {
 }
 /**
  * @audit MinimizeAccessViolation
  */
 public void test() {
   A a=new A();
 }
}
