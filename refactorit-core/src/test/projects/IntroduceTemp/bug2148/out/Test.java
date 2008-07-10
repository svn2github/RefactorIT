interface X {
  public String method();
}

public class Test implements X {
  public String method() {
    final String object = ((X) new Test()).method();
    object.length();
    return "";
  }
}
