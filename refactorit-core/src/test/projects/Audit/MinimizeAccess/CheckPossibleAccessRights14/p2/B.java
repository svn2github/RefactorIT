package CheckPossibleAccessRights14.p2;
import CheckPossibleAccessRights14.p1.A;

public class B extends A {
  private void f2(B ref) {
    ref.f1();
  }
}
