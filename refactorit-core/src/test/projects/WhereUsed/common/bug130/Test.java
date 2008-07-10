class Test {
  public void test(B b) {
    b.test();
  }
}


class A {
  public void test() {}
}

class B extends A {
}