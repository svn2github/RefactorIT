class Test {
  void a(int var1, double var2, float var3) {
    var3 = (int) var1;
  }

  void b(int test1, int test2) {
    int test3 = 0;
  }

  // Don't report final methods
  final void c(double ignoreme1, int ignoreme2) {
    ignoreme1 = 1.0;
  }

  // Don't report static methods
  static void d(int unusedParameter) {
    System.out.println("Hello");
  }

  int e() {
    int i = 0;
    int unusedLocal = 0;

    for (i = 0; i < 10; i++) {
      System.out.println("Hi!");
    }

    return i;
  }
}
