class B {
}

public class A {
  public InnerA a;

  public InnerA f1(InnerA param) {
    InnerA local;
    return param;
  }

// use qualified name of Inner
  public class InnerA {
  }
}
