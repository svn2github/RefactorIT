package p1;

import java.util.*;

public class A {
  public void f1() {
    f2();

    final A a = new A();
    a.f1();
  }

  private void f2() {
    f3();

    A a = new A();
    a.f2();
  }

  public void f4() {
    f2();
    getA().f4();
  }

  public void f3() {
  }

  public A getA() {
    return this;
  }
}
