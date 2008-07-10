interface X {
  void test2();
}

class Test implements X {
  void test() {
    ((X) null).test2();
  }
  
  void test2() {}
}
