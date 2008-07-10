package checkExtends1;

public class A {
  /**
   * @audit MinimizeAccessViolation
   */
  public void f() {
  }
}

class B extends A {
  /**
   * @audit MinimizeAccessViolation
   */
 public void f () {
  //super.f();
 }
}
