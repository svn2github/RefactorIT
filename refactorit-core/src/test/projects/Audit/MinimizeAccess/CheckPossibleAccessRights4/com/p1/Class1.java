package CheckPossibleAccessRights4.com.p1;

public class Class1 {
	protected void f() { }
}

class Class2 extends Class1 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void f() { }
}
