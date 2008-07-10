package p;

public class A {
  B right; 
  
  public void meth(B param) {
    meth1(((param != right)? right : param));
    param.bspecific();
  }

  public void meth1(I param) {
  }
}

interface I {}
class B implements I {
  public void bspecific() { }
}