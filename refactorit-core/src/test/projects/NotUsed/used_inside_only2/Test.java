class Test {
  static final String NAME = Test.class.getName();

  static int f = 13;

  Test() {}

  static void test() {
    System.out.println(f);
    test2();
  }

  static void test2() {
    test();
  }

  void test3() {
    test3();
    Inner i = new Inner();
    i.abc();
    System.out.println(Inner.NAME);
  }

  static class Inner {
    static final String NAME = Inner.class.getName();
    void abc() {
      f = 14;
      test2();
      Test t = new Test();
      t.test();
    }
  }
}
