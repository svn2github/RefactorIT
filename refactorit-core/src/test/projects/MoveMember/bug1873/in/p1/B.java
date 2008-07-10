package p1;

import p2.X;

public class B {
  private A param;

  B(A param) {
    X.f(this.param, this);
  }
}
