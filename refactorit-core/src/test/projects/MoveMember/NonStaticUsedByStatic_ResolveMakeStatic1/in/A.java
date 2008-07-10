public class A {
  private void f1() {
  }

  public static void f2() {
    B b = new B();
    A a = new A();
    a.f1();
  } 
}
