package corrective.Forin.in;

/**
 * @violations 2
 */
public class TestDuplicateNames {
  public void meth1() {
    Integer[] arr = new Integer[10];
    for (int i = 0; i < arr.length; i++) {
      Integer[] subarr = new Integer[12];
      for (int j = 0; j < subarr.length; j++) {
        if (arr[i] == subarr[j]) {
          Integer integer2 = arr[i];
        }
      }
    }
  }
}
