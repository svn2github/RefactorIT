package a;

public static class X {
  // Let's check that array parsing works despite that they implement no interfaces.
  byte[] array;

  public int sum() {
    int result = 0;
    for(int i = 0; i < array.length; i++) {
      result += array[i];
    }
    return result;
  }
}
