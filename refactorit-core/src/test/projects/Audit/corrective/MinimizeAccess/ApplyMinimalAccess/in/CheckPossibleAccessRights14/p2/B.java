package CheckPossibleAccessRights14.p2;
import CheckPossibleAccessRights14.p1.A;

/**
 * @violations 0
 */
public class B extends A {
  private void f2(B ref) {
    ref.f1();
  }
}
