class Test {
  void a() {
  }

  void b() {
    System.out.println("Hello");
    synchronized (this) {
      System.out.println("Hello World!");
    }

    try {
      wait();
    } catch (Exception e) {
      notify();
    }
  }

  void c() {
    b();
    if ((System.currentTimeMillis() > 10) && (1 != (2 + 3))) {
      return;
    }

    if (test1()) {
      return;
    }
  }

  boolean test1() {
   return true;
  }

  int test2() {
    return 3;
  }

  void d() {
    c();
    for (int i = ((3 != 4) ? ((test1()) ? 2 : 1) : 0); test1(); i++) {
      System.out.println("Hi!");
      if (2 != 4) {
        break;
      } else {
        if (5 == 6) {
          continue;
        }
      }

      System.out.println("Test");
    }

    for (;true;) {
    }
  }

  void e() {
    while ((3 != 4) && (5 != 6)) {
      return;
    }

    do {
      System.out.println("Hi!");
    } while ((13 != 14) ? true : false);
  }

  void f() {
    switch (3) {
      case 1:
        System.out.println("Test");
        break;

      case 2:
        if ((3 != 4) || ((5 != 6) && (7 != 8))) {
          System.out.println("Test");
        }

      case 3:
        System.out.println("Test");
        break;

      case 4:
        break;
    }
  }

  void g() {
    switch (4) {
      case 1:
        break;

      default:
        System.out.println("Test");
        break;
    }
  }
}
