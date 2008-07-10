public class Test {
  public static final void main(String[] params) {
    final B b = new B();
    b.test("Hello 2");
  }
}

class A {
  void test(Object o) {System.out.println("A.test(Object)");}
}

class B extends A {
  void test(String s) {System.out.println("B.test(String)");}
}