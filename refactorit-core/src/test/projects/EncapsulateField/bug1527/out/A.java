public class A {
  
  private int test;
  
  private void write() {
    setTest(0);
  }
  
  private void read() {
    int i = getTest();
  }
  
  public int getTest() {
    return test;
  }

  public void setTest(final int test) {
    this.test = test;
  }
}