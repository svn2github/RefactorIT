public class A {
  
  public static int test;
  
  private static void write() {
    A.test = 0;
  }
  
  private static void read() {
    int i = A.test;
  }
}