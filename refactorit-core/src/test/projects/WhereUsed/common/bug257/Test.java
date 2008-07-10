class A {
  int b = 13;
  void test() {
    System.out.println(b);
  }
}

class B extends A {
  void test() {
    System.out.println(b);
  }
}
