
import java.io.IOException;


public class Test5 {

  static void fun() throws IOException {
  }

  public static void main(String args[]) {
    try {
      /*]*/
      newmethod();
      /*[*/
    } catch (Exception e) {
    }
  }

  static void newmethod() throws IOException {
    fun();
  }
}
