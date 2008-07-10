// tests constructor usage
interface I {


}

class A implements I {
  public void m() {

  }
}

class B {
  A a;
  B(A a) {
   a.m();
  }

  void test() {
   B b=new B(a);
  }


}

