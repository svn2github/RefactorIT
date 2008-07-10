class A {
  void test() {
    m();
  }

  static void m() {}
}

class B extends A {
  void test() {
    m();
  }
}
