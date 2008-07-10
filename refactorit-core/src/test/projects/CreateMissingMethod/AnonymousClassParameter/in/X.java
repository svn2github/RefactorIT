public class X {
  public void m() {
    missingMethod(new X() {});

    withInterface(new java.io.Serializable() {});

    withClassThatImplementsInterfaces(new ClassWithInterface() {});
  }

  private static class ClassWithInterface implements java.io.Serializable {
  }
}
