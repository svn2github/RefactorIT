public class A {
  
  int test;
  
  private void write1() {
    test = (1 + (7 * 5) + 1) - 4;
  }

  private void write2() {
    test = (1 + (7 * test) + 1);
  }

  private void write3() {
    test += 5;
    test *= 5;
  }

  private void incdec() {
    test ++;
    test --;
    ++test;
    --test;
    this.test++;
  }

  private void read1() {
    int i = test + 2 * test + 1;
  }

  private void readAndWrite1() {
    test = test + 1;
  }

  private void read2() {
    test = (new int[] {1, 2, 3})[1];
  }

  private void read3() {
    test = Integer.parseInt("1");
  }
}