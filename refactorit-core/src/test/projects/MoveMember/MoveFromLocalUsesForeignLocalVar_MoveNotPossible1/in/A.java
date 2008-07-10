public class A {
  public void f1() {
    final int a = 1;

    class Local {
      public void local1() {
        int b = a;
      }
    }
  }
}

class B {
}


