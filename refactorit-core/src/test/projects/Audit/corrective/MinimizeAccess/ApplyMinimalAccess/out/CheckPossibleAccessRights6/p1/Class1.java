package CheckPossibleAccessRights6.p1;
import CheckPossibleAccessRights6.p2.Class10;

/**
 * @violations 2
 */
public class Class1 {
	protected void f() { }
}

class Class2 extends Class10 {
	private void func() { 
		f();
	}
}

