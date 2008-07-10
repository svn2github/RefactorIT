
public class Test {

  public void method() {
    int field = 1;

    class XXX {
      int field = 2;

      public void localMethod() {
        field = 3;
      }
    }
  }
}
