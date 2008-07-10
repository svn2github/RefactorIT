public class A {
  public void method(B b) {
    b.y = b.x;
    if (++b.x == 0) {
    }
  }
}

class B {
  int x = 0;
  int y;
}

class C {
  B b1 = null;

  static {
    A a = null;
    B b2 = null;

    a.method(b1);
    a.method(b2);
  }
}
