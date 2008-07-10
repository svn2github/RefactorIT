import java.io.Serializable;


public class X {
  public void m() {
    missingMethod(new X() {});

    withInterface(new java.io.Serializable() {});

    withClassThatImplementsInterfaces(new ClassWithInterface() {});
  }

  private static class ClassWithInterface implements java.io.Serializable {
  }

  public void missingMethod(X param0) {
    //FIXME: implement this
    throw new java.lang.UnsupportedOperationException("Method missingMethod() not implemented");
  }

  public void withInterface(Serializable param0) {
    //FIXME: implement this
    throw new java.lang.UnsupportedOperationException("Method withInterface() not implemented");
  }

  public void withClassThatImplementsInterfaces(ClassWithInterface param0) {
    //FIXME: implement this
    throw new java.lang.UnsupportedOperationException("Method withClassThatImplementsInterfaces() not implemented");
  }
}
