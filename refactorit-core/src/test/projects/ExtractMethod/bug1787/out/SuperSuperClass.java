public class SuperSuperClass {
  public void myMethod() {
    for( int i = 0; i < 10; i++ ) {
      /*]*/
      String usedLater = newmethod();
      /*[*/

      System.err.println(usedLater);
    }
  }

  String newmethod() {
    String usedLater = "";

    new Runnable() {
      public void run() {
        String notUsedLater = "";
        System.err.println(notUsedLater);
      }
    };

    return usedLater;
  }
}
