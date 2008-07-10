public class C extends B {
  public C() {
  }

  public void method(int aaa, int bbb, int ccc) {
    int d = aaa;
    aaa = bbb - ccc;
    bbb = ccc / d;
  }

}
