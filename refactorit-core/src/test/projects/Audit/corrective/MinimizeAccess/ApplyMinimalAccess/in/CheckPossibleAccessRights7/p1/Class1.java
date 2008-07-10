package CheckPossibleAccessRights7.p1;
import CheckPossibleAccessRights7.p2.Class10;

class Class0 {
	public void f() { }
}

/**
 * @violations 3
 */
public class Class1 extends Class0 {
}

class Class2 extends Class10 {
	public void func() { 
		f();
	}
}

class Class3 extends Class0 {
	public void func() {
		f();
	}
}
