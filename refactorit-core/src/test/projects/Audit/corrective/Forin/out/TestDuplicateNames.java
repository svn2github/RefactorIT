package corrective.Forin.in;

/**
 * @violations 2
 */
public class TestDuplicateNames {
  public void meth1() {
    Integer[] arr = new Integer[10];
    for (Integer integer : arr) {
      Integer[] subarr = new Integer[12];
      for (Integer integer3 : subarr) {
        if (integer == integer3) {
          Integer integer2 = integer;
        }
      }
    }
  }
}
