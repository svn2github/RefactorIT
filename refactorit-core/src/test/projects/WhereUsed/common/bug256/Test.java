interface MyInterface {
  int a = 1;
}

class MyClass implements MyInterface {
  void test() {
    System.out.println(a);
  }
}

class MyOtherClass implements MyInterface {
  void test() {
    System.out.println(a);
  }
}
