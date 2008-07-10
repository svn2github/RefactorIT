package CheckPossibleAccessIfUsedInAnonymous.a;
/**
 * @violations 8
 */
public class A {
  public int a;
  public int b;
  public int c;
  public int d;
  public int e;
  public int f;
  public int g;
  public int h;
  public int i;
  public int j;

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
