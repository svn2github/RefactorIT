
public class Test {

  public void method() {
    int y = 0;

    if (true) {
      int x = y;
    } else {
      /*]*/int z = y;/*[*/
    }
  }

}
