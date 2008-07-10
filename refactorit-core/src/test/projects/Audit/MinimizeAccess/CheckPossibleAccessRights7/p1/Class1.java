package CheckPossibleAccessRights7.p1;
import CheckPossibleAccessRights7.p2.Class10;

class Class0 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void f() { }
}

public class Class1 extends Class0 {
}

class Class2 extends Class10 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void func() { 
		f();
	}
}

class Class3 extends Class0 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void func() {
		f();
	}
}
