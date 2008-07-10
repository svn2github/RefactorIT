package CheckPossibleAccessRights3.com.p1;
/**
 * @violations 2
 */
public class Class1 {
	protected void f() { }
}

class Class2 extends Class1 {
	protected void f() { }

	private void foo() {
		f();
	}
}
