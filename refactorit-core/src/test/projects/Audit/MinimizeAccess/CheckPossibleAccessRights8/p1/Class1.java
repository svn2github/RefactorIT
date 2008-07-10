package CheckPossibleAccessRights8.p1;

public class Class1 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void f() { }
}

class Class2 extends Class1 {
}


class Class3 extends Class2 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void func() { 
		f();
	}
}
