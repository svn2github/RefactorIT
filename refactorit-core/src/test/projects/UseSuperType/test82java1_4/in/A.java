package p;

/**
 * Java 1.4 by default, conditional expression arguments of 
 * different parameters are not allowed.
 */
public class A {
  B right; // type can not be changed to I
  
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