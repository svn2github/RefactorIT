public class Test {
  static{
    X x;
    A a;
    B b;
    C c;
  }
}

interface X{
  void method();
}

class A implements X {
  public void method() {
  }
}

class B extends A {
  public void method() {
    super.method(); // here it uses A.method() only
  }
}

class C implements X {
  public void method() {
  }
}