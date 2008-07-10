package CheckPossibleAccessRights7.p1;
import CheckPossibleAccessRights7.p2.Class10;

class Class0 {
	protected void f() { }
}

/**
 * @violations 3
 */
public class Class1 extends Class0 {
}

class Class2 extends Class10 {
	private void func() { 
		f();
	}
}

class Class3 extends Class0 {
	private void func() {
		f();
	}
}
