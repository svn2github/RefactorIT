package LCOM;

public final class LCOMtest1 {
  public static final int nField1 = 0;
  private static final int nField2 = 1;
  public boolean bField1 = false;
  private boolean bField2 = true;

  public void method1(int test) {
    bField1 = true;
    bField1 = false;
    bField2 = true;
    test = 2;
  }

  private final void method2() {
    bField2 = false;
  }

  public int method3() {
    return 0;
  }

  final class Innerfields {
    private int nInnerfield = 0;

    void innerMethod1() {
      nInnerfield = 1;
    }

    void innerMethod2() {
      nInnerfield = 0;
    }

    void innerMethod3() {
      // Comment
    }
  }
}
