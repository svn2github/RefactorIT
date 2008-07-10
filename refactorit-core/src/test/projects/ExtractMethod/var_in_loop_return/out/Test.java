
public class Test {

  public void extractArea() {
    for (int i = 0; i < 10; ++i) {
      /*]*/int z = newmethod();/*[*/
      ++z;
    }
  }

  int newmethod() {
    int z = 1;
    int a = 1;
    z = z * 1 * a;

    return z;
  }

}
