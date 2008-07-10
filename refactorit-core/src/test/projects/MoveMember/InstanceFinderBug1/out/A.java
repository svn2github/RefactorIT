class Super {
}

public class A extends Super {
}

class B extends A {

  public void f() {
  }
}

class C {
  public void f(Super ref) {
    if (ref instanceof B) {
      ((B)ref).f();
    }
  }
}
