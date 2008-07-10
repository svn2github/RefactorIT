class B {

  public InnerB b;
  public class InnerB {
  }

  public InnerB f1(InnerB param) {
    InnerB local;
    return param;
  }
}

public class A {
}
