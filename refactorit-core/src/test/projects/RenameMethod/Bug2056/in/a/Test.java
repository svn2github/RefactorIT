package a;

public class Test {
  public Object m() {
    class XXX {
      public Object something() {
        return somethingWithString("s");
      }
    };

    return null;
  }

  public Object somethingWithString(String s) {
    return null;
  }
}
