import java.text.SimpleDateFormat;

public class Test {
  private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

  public void dummy(java.util.Date date) {
    try {
     System.out.println(format.format(date));
     A a = new A();
    } finally {
      return;
    }
  }
}

class A {
  {
    Test t = new Test();
  }
}
