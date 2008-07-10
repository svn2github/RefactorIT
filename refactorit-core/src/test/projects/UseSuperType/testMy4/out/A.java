// tests constructor usage in new
interface I {


}

class A implements I {
  public void m() {

  }
}

class B {
  A a;
  B(A a) {
   this.a=a;
   this.a.m();
  }

  static void test() {
   A a=new A();
   B b=new B(a);

  }


}

