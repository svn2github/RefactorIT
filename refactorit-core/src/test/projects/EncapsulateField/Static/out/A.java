public class A {
  
  public static int test;
  
  private static void write() {
    A.setTest(0);
  }
  
  private static void read() {
    int i = A.getTest();
  }

  public static int getTest() {
    return A.test;
  }

  public static void setTest(final int test) {
    A.test = test;
  }
}