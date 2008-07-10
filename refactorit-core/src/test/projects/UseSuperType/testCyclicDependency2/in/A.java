// @author tonis
// needs cyclic dependency resolving
interface I {

}

class A implements I {
}

class B  {

 A test2(A a) {
    test(a);
    return a;
 }

  void test(A a) {
    A a1=test2(a);

  }

}

