// @author tonis
// needs cyclic dependency resolving
// this contains infinite loop but doesn't matter

interface I {

}

class A implements I {
  void test2(I a) {
    test(a);
  }

  void test(I a) {
    test2(a);
  }


}

