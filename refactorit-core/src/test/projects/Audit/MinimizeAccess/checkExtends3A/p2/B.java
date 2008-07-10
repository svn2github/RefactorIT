package checkExtends3A.p2;
import checkExtends3A.A;

class B extends A {
  /**
   * @audit MinimizeAccessViolation
   */
 public void f () {
  //super.f();
}
}
