package p1;

class B {
  {
    method();
  }

  public void method() {}
}

class C {
  static B b = null;
  static {
    b.method();
    B bb = null;
    A a = null;
    bb.method();
  }

  public void xxx(A aaa, B bbb) {
    bbb.method();
  }
}

public class A {

  public void methodX() {
    class X {
      B bbbb = null;
      
      {
        bbbb.method();
      }
    }
  }
}
