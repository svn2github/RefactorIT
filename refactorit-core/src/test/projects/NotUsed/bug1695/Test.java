class A {
  void test() {}
}

class B extends A {
}

class Test {
  {
    B b = new B();
    b.test();
  }

  public static void main(String[] args) {
    new Test();
  }
}
