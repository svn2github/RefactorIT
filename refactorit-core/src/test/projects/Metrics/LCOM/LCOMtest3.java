package LCOM;

public final class LCOMtest3 {
  public static final int nField1 = 0;
  private static int nField2 = 1;
  public boolean bField1 = false;
  private boolean bField2 = true;

  public void method1(int test) {
    bField1 = true;
    bField1 = false;
    bField2 = true;
    test = 2;
  }

  private final void method2() {
    nField2 = 0;
  }

  public int method3() {
    return nField2;
  }
}
