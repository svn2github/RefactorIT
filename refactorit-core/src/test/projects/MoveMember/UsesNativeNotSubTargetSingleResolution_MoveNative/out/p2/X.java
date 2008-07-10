package p2;

public class X {

  public void f1() {
    f2();
  }

  private void f2() { }

/***************/

  public static void f4() {
    f5();
  }

  private static void f5() { }

/***************/

  public void f6() {
    f7();
  }

  private static void f7() { }
}
