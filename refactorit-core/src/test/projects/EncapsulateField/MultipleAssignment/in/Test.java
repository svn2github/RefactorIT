public class Test {
  int i;
  int z;

  void method1(int g) {
    int j = i = 10;
    int a = i = getSmth(5 + 6);
    int b = a = g = i;
    int c = i = b = g;
    int o = i + 3;
    i++;
    i += 3;
    i = j + 3;
    i = i + 2;

    for (int p = o = i = 2; true; p = i = 5) {

    }
  }

  void method2() {
    int m = 0;
    int j = m = i = 2;
    boolean bool = m < i;
    int b;

    while ((b = i) == 2) {
    }

    if (b == (i = j)) {
    }
  }

  public int getSmth(int a) {
    return a * a;
  }
}
