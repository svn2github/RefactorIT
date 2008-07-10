package CheckPossibleAccessIfUsedInAnonymous.a;
/**
 * @violations 8
 */
public class A {
  private int a;
  private int b;
  private int c;
  public int d;
  public int e;
  protected int f;
  protected int g;
  protected int h;
  protected int i;
  protected int j;

  private void f1() {
    new Runnable() {
      public void run() {
        a = 1;

        class Local {
          public void method() {
            c = 3;
          }
        }
      }
    };
  }

  private void f2() {
    class MyRunnable implements Runnable {
      public void run() {
        b = 2;
      }
    }
  }
}
