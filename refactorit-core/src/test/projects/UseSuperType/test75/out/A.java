package p;

interface I1 { }
interface I2 {
  public void contains(I2 i);
}

public class A implements I2 {
  public void contains(I2 i) {
  }
}

class B extends A implements I1 {
  public void contains(I1 b) {
    
  }
}
