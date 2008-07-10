package checkExtends1;
/**
 * @violations 2
 */
public class A {
  public void f() {
  }
}

class B extends A {
 public void f () {
  //super.f();
 }
}
