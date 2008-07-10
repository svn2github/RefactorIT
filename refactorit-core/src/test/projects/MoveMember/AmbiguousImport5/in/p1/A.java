package p1;

import java.util.*;
import java.util.ArrayList;

public class A {
  public void f1() {
    B b = new B();
    b.f();
  }

  public void f2() {
    Collections.max(new ArrayList());
  }
}

class B {
  public void f() {
  }
}
