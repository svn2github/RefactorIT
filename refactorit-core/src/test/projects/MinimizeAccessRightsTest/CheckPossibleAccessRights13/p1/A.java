package p1;

public class A {
  public int a1, a2, a3;
  public int a4, a5, a6;
}

class B {
  {
    new A().a2 = 1;
  }
}
