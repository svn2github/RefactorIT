public class B extends A {
  public B() {
  }

  public void method(int a, int b, boolean c) {
    int d = a;
    a = b;
    b = d;
  }

}
