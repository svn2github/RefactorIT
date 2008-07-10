package p1;

public class B {

  public static void f1(A a1) {
    f2(a1);

    final A a = new A();
    f1(a);
  }

  public static void f2(A a1) {
    a1.f3();

    A a = new A();
    f2(a);
  }
}
