class B {
  public class InnerB {
  }
}

public class A {
  public B.InnerB b;

  public B.InnerB f1(B.InnerB param) {
    B.InnerB local;
    return param;
  }
}
