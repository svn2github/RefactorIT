public class C extends B {
  public C() {
  }

  public void method(int aa, int bb, int cc) {
    int d = aa;
    aa = bb - cc;
    bb = cc / d;
  }

}
