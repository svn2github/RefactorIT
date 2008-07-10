public class A {

  int test;

  private void write() {
    set_test(0);
  }

  private void read() {
    int i = get_test();
  }

  public int get_test() {
    return this.test;
  }

  public void set_test(final int test) {
    this.test = test;
  }
}