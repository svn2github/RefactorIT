package p;

public class A {
  I right;
  
  public void meth(I left) {
    I b  = (left != right)? right : left;
  }
}

interface I {}
class B implements I {
  public void bspecific() { }
}
