package CheckPossibleAccessRights6.p1;
import CheckPossibleAccessRights6.p2.Class10;

/**
 * @violations 2
 */
public class Class1 {
	public void f() { }
}

class Class2 extends Class10 {
	public void func() { 
		f();
	}
}

