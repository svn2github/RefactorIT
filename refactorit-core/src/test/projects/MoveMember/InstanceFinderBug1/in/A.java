class Super {
}

public class A extends Super {
  public void f() {
  }
}

class B extends A {
}

class C {
  public void f(Super ref) {
    if (ref instanceof B) {
      ((B)ref).f();
    }
  }
}
