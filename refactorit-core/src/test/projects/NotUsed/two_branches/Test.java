class A {
  void test() {}
}

class B1 extends A {}

class C1 extends B1 {
  void test() {}
}

class B2 extends A {
}

class C2 extends B2 {
  void test() {}
}

class D1 extends C1 {
  void test() {}
}

class Test {
  Test(int a) {}
  
  {
    B1 b = new C1();
    b.test();
  }
}