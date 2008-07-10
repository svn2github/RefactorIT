package p1;

class B {
  {
    new A().method();
  }
}

class C {
  static B b = null;
  static {
    new p1.A().method();
    B bb = null;
    A a = null;
    a.method();
  }

  public void xxx(A aaa, B bbb) {
    aaa.method();
  }
}

public class A {

  public void method() {}

  public void methodX() {
    class X {
      B bbbb = null;
      
      {
        new A().method();
      }
    }
  }
}
