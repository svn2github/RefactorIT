public class A {
}

class B {
  int x = 0;
  int y;

  public void method() {
    this.y = this.x;
    if (++this.x == 0) {
    }
  }
}

class C {
  B b1 = null;

  static {
    A a = null;
    B b2 = null;

    b1.method();
    b2.method();
  }
}
