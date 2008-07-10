public class Test {
  public void test() {
    final Object tmp = new B();
    final Object tmp2 = new A();
  }
}

class A {
  public A() {}

  public A(int a) {}
}

class B extends A {
}

class C extends A {
  public C(int a) {
  }
}

class D extends A {
  public D(int v) {
    super();
  }
  
  public D(int a, int b) {
    super(a);
  }
}