public class Test {
  int i;
  int z;

  void method1(int g) {
    setI(10);
    int j = getI();
    setI(getSmth(5 + 6));
    int a = getI();
    int b = a = g = getI();
    setI(b = g);
    int c = getI();
    int o = getI() + 3;
    setI(getI() + 1);
    setI(getI() + 3);
    setI(j + 3);
    setI(getI() + 2);

    setI(2);
    for (int p = o = getI(); true; p = i = 5) {

    }
  }

  void method2() {
    int m = 0;
    setI(2);
    int j = m = getI();
    boolean bool = m < getI();
    int b;

    while ((b = getI()) == 2) {
    }

    setI(j);
    if (b == (getI())) {
    }
  }

  public int getSmth(int a) {
    return a * a;
  }

  public int getI() {
    return this.i;
  }

  public void setI(final int i) {
    this.i = i;
  }
}
