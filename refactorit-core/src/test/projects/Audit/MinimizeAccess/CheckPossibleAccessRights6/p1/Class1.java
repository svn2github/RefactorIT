package CheckPossibleAccessRights6.p1;
import CheckPossibleAccessRights6.p2.Class10;

public class Class1 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void f() { }
}

class Class2 extends Class10 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void func() { 
		f();
	}
}

