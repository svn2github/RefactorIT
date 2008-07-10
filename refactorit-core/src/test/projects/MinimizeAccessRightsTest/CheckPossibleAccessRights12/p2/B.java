package p2;

import p1.A;

public class B extends A {
  public B(A a) {
    super(a.isShowSource());
  }
}
