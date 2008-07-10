interface I { }

public class A extends B implements I {
  public void aspecific() {}
}

class B extends C{}

class C { }

class D {
  public void meth(C c) {
    
  }
  
  public void meth(A a) {
    
  }
  
  public void method(A a) {
    this.meth(a);
    a.aspecific();
  }
}