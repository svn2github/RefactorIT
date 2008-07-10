package CheckPossibleAccessRights1.com.p2;
import CheckPossibleAccessRights1.com.p1.Class1;

public class Class20 extends Class1 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void f200() {
		tmp1_6 = 1;
		tmp2_6 = 2;
	}
}

public class Class21 {
  /**
   * @audit MinimizeAccessViolation
   */
	public void f201() {
		new Class1().tmp1_7 = 1;
	}
}

