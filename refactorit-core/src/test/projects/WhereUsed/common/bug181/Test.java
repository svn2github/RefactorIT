class Test {
  void test1() throws A, B {}
  void test2() throws Throwable {}
  void test3() throws B {}
  void test4() throws A {}
  void test5() throws B, B {}
}

class A extends RuntimeException {}
class B extends A {}