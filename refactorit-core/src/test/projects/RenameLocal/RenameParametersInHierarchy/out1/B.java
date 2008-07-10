public class B extends A {
  public B() {
  }

  public void method(int aa, int bb, boolean cc) {
    int d = aa;
    aa = bb;
    bb = d;
  }

}
