public class Test {
  public static final void main(String params[]) {  
    B b = new B();
    B.C c = b.new C();
    c.test();
  }
}

class A {
  void test() {System.out.println("Hello from A");}
}

class B extends A {
  void test() {System.out.println("Hello from B");}
  class C {
    void test() {
     B.this.test();
     B.super.test();
   }
  }
}
