package p2;
import p1.A;

public class B extends A {
  private void f2(B ref) {
    ref.f1();
  }
}
