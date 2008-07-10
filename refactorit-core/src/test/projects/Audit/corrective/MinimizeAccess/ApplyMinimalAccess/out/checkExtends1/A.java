package checkExtends1;
/**
 * @violations 2
 */
public class A {
  void f() {
  }
}

class B extends A {
 void f () {
  //super.f();
 }
}
