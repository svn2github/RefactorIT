package CheckPossibleAccessRights11.p1;
import java.util.*;

interface Interface1 {
	void f1();
}

class Class0 {
	void f1() { }
	void f2() { }
	private void f3() { }
}

public class Class1 extends Class0 implements Interface1{
  public void f1() { }
  /**
   * @audit MinimizeAccessViolation
   */
	public void f2() { }
  /**
   * @audit MinimizeAccessViolation
   */
	public void f3() { }
}
