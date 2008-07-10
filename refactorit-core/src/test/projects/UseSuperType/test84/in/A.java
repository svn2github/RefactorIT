package p;

interface I2 extends I1 { 
  public void i2specific();
}

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

class D extends A implements IHierarchy {
  public I2 meth() {
    return null;
  }
}

class E extends D {
  public I2 meth() {
    return null;
  }
}

class F {
  public void method(E e) {
    e.meth().i2specific();
  }
}