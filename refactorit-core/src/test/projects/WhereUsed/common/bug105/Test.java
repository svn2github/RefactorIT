public class Test {
  public void abc() {
    Runnable r = new Runnable() {
      public void run() {
        test();
        Runnable tmp = new Runnable() {
          public void run() {
            test();
          }
        };
      }
    };
  }
  
  public void test() {
  }
}