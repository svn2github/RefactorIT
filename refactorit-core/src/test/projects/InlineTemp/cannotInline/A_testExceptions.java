package test.projects.InlineTemp.cannotInline;

import java.io.FileInputStream;
import java.io.IOException;

class A {
  void f() throws IOException {
    final FileInputStream in = new FileInputStream("x.txt");
    
    new Runnable() {
      public void run() {
        in.toString();
      }
    };
  }
}
