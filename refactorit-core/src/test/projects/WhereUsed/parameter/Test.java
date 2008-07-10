class Test {
  int a = 13;

  int test(final int a) {
    new Runnable() {
      public void run() {
        System.out.println(a);
      }

      private void test() {
        int a = 9;
      }
    }.run();
    final int b = this.a;
    return (a + b * 2);
  }

  int test2(int a) {
    for (a = 0; a < --a; a++) {
      if (a < this.a) {
        return a;
      }
    }

    throw new RuntimeException(""
      + a);
  }

  void test3(String s) {
    throw new RuntimeException("" + s.toString());
  }

  void test4(Test t) {
    throw new RuntimeException("" + t.a);
  }

  void test5(Object[] arr) {
    arr[1] = arr[0];
    throw new RuntimeException(arr[2].toString());
  }

  public static final void main(String[] params) {
    Test test = new Test();
    System.out.println(test.test(5));
  }
}
