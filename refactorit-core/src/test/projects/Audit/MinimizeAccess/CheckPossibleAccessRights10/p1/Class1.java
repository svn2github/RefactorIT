package CheckPossibleAccessRights10.p1;

public class Class1 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void f1() {
	}

  /**
   * @audit MinimizeAccessViolation
   */
	public void f2() {
		f1();
	}
}
