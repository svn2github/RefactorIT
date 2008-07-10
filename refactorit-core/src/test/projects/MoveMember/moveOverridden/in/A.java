package moveOverridden;

public class A {

  void test1() {
  }
}

class B extends A {

}
class C extends B {


  void test1() {
  }

  void test2() {
  test1();
  }
}

class D {

}