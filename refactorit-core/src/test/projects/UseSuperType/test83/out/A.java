package p;

interface I2 extends I1 { }

interface I1 { }

public abstract class A {
  public abstract I1 meth();
}

final class B extends A {

  public I1 meth() {
    return null;
  }
}

final class C extends A {
  public I1 meth() {
    return null;
  }
}

interface IHierarchy {
  public I1 meth();
}

class D implements IHierarchy {
  public I1 meth() {
    return null;
  }
}

class E implements IHierarchy {
  public I1 meth() {
    return null;
  }
}