public class A {

  public void f1() {
    class Local {
      public void local1(Local o) {
        o.local2();
      }

      public void local2() {
      }
    }
  }
}

class B {
}
