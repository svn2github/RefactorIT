package CheckExplicitConstructorCall.p2;

import CheckExplicitConstructorCall.p1.A;

/**
 * @violations 2
 */
public class B extends CheckExplicitConstructorCall.p1.A {
  
 public B() {
 }

 public void test() {
   A a=new A();
 }
}
