package CheckExplicitConstructorCall.p2;

import CheckExplicitConstructorCall.p1.A;

/**
 * @violations 2
 */
public class B extends CheckExplicitConstructorCall.p1.A {
  
 private B() {
 }

 private void test() {
   A a=new A();
 }
}
