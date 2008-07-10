package CheckPossibleAccessIfUsedInAnonymous.a;

public class A {
  /**
   * @audit MinimizeAccessViolation
   */
  public int a;
  /**
   * @audit MinimizeAccessViolation
   */
  public int b;
  /**
   * @audit MinimizeAccessViolation
   */
  public int c;
  public int d;
  public int e;
  /**
   * @audit MinimizeAccessViolation
   */
  public int f;
  /**
   * @audit MinimizeAccessViolation
   */
  public int g;
  /**
   * @audit MinimizeAccessViolation
   */
  public int h;
  /**
   * @audit MinimizeAccessViolation
   */
  public int i;
  /**
   * @audit MinimizeAccessViolation
   */
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
