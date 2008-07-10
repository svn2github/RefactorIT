package p1;

public class A {
  private void f() {
    new Runnable() {
      public void run() {
        B a;
      }
    };
  }
}
