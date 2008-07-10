// @author tonis
// needs cyclic dependency resolving
interface I {

}

class A implements I {
}

class B  {

 I test2(I a) {
    test(a);
    return a;
 }

  void test(I a) {
    I a1=test2(a);

  }

}

