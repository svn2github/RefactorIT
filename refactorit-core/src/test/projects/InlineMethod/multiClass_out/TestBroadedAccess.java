public class TestBroadedAccess {
	static int FOO = 0;
	
	public void bar() {
		FOO = 1;
	}
}

class X {
	void m() {
		TestBroadedAccess t = new TestBroadedAccess();
		TestBroadedAccess.FOO = 1;
	}
}