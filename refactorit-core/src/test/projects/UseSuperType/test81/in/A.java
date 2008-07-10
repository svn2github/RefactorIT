package p;

public class A {
  B right;  
  
  public void meth(B param) {
    meth1(((param != right)? right : param));
  }
  
  public void meth1(B param) {
    param.bspecific();
  }
}

interface I {}
class B implements I {
  public void bspecific() { }
}
