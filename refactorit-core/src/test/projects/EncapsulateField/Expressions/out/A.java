public class A {
  
  private int test;
  
  private void write1() {
    setTest((1 + (7 * 5) + 1) - 4);
  }

  private void write2() {
    setTest((1 + (7 * getTest()) + 1));
  }

  private void write3() {
    setTest(getTest() + 5);
    setTest(getTest() * 5);
  }

  private void incdec() {
    setTest(getTest() + 1);
    setTest(getTest() - 1);
    setTest(getTest() + 1);
    setTest(getTest() - 1);
    this.setTest(this.getTest() + 1);
  }

  private void read1() {
    int i = getTest() + 2 * getTest() + 1;
  }

  private void readAndWrite1() {
    setTest(getTest() + 1);
  }

  private void read2() {
    setTest((new int[] {1, 2, 3})[1]);
  }

  private void read3() {
    setTest(Integer.parseInt("1"));
  }

  public int getTest() {
    return this.test;
  }

  public void setTest(final int test) {
    this.test = test;
  }
}