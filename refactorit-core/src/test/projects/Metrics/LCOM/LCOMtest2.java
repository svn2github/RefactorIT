package LCOM;

public final class LCOMtest2 {
  public static final int nField1 = 0;
  private static final int nField2 = 1;
  public boolean bField1 = false;

  public void method1(int test) {
    test = 2;
  }

  private final void method2() {
    bField1 = false;
  }

  public int method3() {
    return nField1;
  }

  final class Innerfields {
    private final int nInnerfield = 0;
    boolean bField1;
  }
}
