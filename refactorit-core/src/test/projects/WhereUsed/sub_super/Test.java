class Test extends B {
  static {
    (new A()).test();
    (new B()).test();
    (new Test()).test();
  }
  
  public void test() {}
}


class A {
  public void test() {}
}

class B extends A {
  public void test() {}
}