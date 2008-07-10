
public class A implements X {

  public static class AEntry implements X.Entry {
  }

  public X.Entry getEntry() {
    return new AEntry();
  }

}
