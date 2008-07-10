package p;

public class A {
  I right;  
  
  public void meth(I param) {
    meth1(((param != right)? right : param));
  }
  
  public void meth1(I param) {
  }
}

interface I {}
class B implements I {
  public void bspecific() { }
}
