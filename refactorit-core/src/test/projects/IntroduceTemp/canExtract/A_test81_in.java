public class A {
	void test(boolean flag) {
		Object o = new Object();
		String s = "foo";
		Object tmpO = flag ? s : o;
	}
}