package p1;

import p2.B;


public class A {
  void usesInner() {
    B.Inner inner = new B.Inner();
    inner.foo();
    int b = B.Inner.BAR;
  }
}