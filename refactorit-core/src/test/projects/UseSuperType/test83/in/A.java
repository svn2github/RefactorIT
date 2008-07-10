package p;

interface I2 extends I1 { }

interface I1 { }

public abstract class A {
  public abstract I2 meth();
}

final class B extends A {

  public I2 meth() {
    return null;
  }
}

final class C extends A {
  public I2 meth() {
    return null;
  }
}

interface IHierarchy {
  public I2 meth();
}

class D implements IHierarchy {
  public I2 meth() {
    return null;
  }
}

class E implements IHierarchy {
  public I2 meth() {
    return null;
  }
}