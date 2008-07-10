class B {

  public A.InnerA a;

  public A.InnerA f1(A.InnerA param) {
    A.InnerA local;
    return param;
  }
}

public class A {

// use qualified name of Inner
  public class InnerA {
  }
}
