import java.io.*;


public class TestException {

  void f() throws IOException {
    final FileInputStream in = new FileInputStream("x.txt");

    new Runnable() {
      public void run() {
        in.toString();
      }
    };
  }

}
