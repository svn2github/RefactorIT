public class TestBroadedAccess {
	private static int FOO = 0;
	
	public void bar() {
		FOO = 1;
	}
}

class X {
	void m() {
		TestBroadedAccess t = new TestBroadedAccess();
		/*[*/t.bar()/*]*/;
	}
}