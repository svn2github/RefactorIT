package CheckPossibleAccessRights8.p1;

/**
 * @violations 2
 */
public class Class1 {
	void f() { }
}

class Class2 extends Class1 {
}

class Class3 extends Class2 {
	private void func() { 
		f();
	}
}
