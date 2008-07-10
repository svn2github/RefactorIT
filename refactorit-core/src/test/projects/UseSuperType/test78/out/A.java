package p;

public class A {
  B right;
  
  public void meth(B left) {
    B b  = (left != right)? right : left;
    b.bspecific();
  }
}

interface I {}
class B implements I {
  public void bspecific() { }
}
