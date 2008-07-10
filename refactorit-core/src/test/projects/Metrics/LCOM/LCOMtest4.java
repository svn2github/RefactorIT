package LCOM;

public final class LCOMtest4 {
  public static int nField1 = 0;
  private static int nField2 = 1;

  public void method1(int test) {
    nField2 = test;
  }

  private final void method2() {
    nField1 = nField2;
  }

  public int method3() {
    return 0;
  }

  public int method4() {
    return 0;
  }

  public int method5() {
    return 0;
  }
}
