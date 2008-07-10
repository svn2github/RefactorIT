public class A {

  public void f1() {
    class Local {
      public void local1() {
        local2();
      }

      public void local2() {
      }
    }
  }
}

class B {
  public void local2() { }
}
