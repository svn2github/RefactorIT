interface X {
  public String method();
}

public class Test implements X {
  public String method() {
    ((X) new Test()).method().length();
    return "";
  }
}
