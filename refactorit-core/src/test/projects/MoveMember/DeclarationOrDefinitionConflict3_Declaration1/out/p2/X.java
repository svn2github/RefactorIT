package p2;

import p1.C;
import p1.D;
import p1.B;

public class X extends B {

  protected C f1(D ref) {
    throw new RuntimeException("method f1 is not implemented");
  }
}
