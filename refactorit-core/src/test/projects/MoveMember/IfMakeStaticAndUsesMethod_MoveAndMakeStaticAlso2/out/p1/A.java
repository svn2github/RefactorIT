package p1;

import java.util.*;

public class A {

  public void f4() {
    B.f2(this);
    getA().f4();
  }

  public void f3() {
  }

  public A getA() {
    return this;
  }
}
