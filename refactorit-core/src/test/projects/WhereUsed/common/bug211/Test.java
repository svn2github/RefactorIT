import a.B;
import a.*;

class Test {
  {
    try {
      if (Class.forName("A").newInstance() instanceof A);
    } catch (Exception e) {}
  }

  static {
    System.out.println(B.class);
  }
}