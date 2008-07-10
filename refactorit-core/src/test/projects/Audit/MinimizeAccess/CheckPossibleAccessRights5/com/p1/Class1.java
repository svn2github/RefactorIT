package CheckPossibleAccessRights5.com.p1;

public class Class1 {
	void f() { }
}


class Class2 extends Class1 {
  /**
   * @audit MinimizeAccessViolation
   */
	protected void f() { }
}

class Class3 extends Class2 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void f() { }
}
