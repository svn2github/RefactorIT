public class Test {

  public void method() {
    if (true) {

    } else if (true) { // this is what causes troubles
      /*]*/
      String args = new String();
      /*[*/

      System.out.println(args);
    }
  }
}
