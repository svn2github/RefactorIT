import p1.A;


public class B {

  public void method(A a) {
    a.field++;
  }

  public void method2(A a) {
    method(a);
  }
}
