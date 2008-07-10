public class A {
  public void f() {
    class Local1 {
      public void local1() { }
    }

    class Local2 {
      public void local2() {
        new Local1().local1();
      }
    }
  }
}
