package CheckPossibleAccessRights3.com.p1;
/**
 * @violations 2
 */
public class Class1 {
	void f() { }
}

class Class2 extends Class1 {
	void f() { }

	private void foo() {
		f();
	}
}
