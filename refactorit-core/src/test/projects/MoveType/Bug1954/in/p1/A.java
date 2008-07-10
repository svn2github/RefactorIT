package p1;

public class A {

  private static class Inner {
    public static void f2() { }
  }

  public void f1() {
    Inner.f2();
  }
}
