public class Test {
  private Object tmp = new Object() {
    public int hashCode() { return 0; }
  };

  public static class Inner1 {
    public abstract class Inner {
      {
        abstract class Local {}
      }
    }

    private class Inner2 {}
  }

  protected static abstract class Inner2 {
    public class Inner {}

    private class Inner3 {}

    public interface SomeInterface {}
  }

  class Inner3 extends Inner2 {
    public class Inner {}
  }

  private static class Inner4 {

    {
      new Runnable() {
        public void run() {
          class Local {
            public class Inner {
            }
          }
        }
      }.run();
    }
  }

}

interface SomeInterface {
  Runnable r = new Runnable() { public void run() {}};

  class Test {}
}

public interface ExportedInterface {
  public class Test {}
  interface I {}
}

interface InternalInterface {
  public interface I {}
}