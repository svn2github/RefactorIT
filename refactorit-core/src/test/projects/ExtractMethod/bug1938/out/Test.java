public class Test {

  public void method() {
    if (true) {

    } else if (true) { // this is what causes troubles
      /*]*/
      String args = newmethod();
      /*[*/

      System.out.println(args);
    }
  }

  String newmethod() {
    String args = new String();

    return args;
  }
}
