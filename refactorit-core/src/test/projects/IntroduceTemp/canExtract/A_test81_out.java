public class A {
	void test(boolean flag) {
		Object o = new Object();
		String s = "foo";
		Object temp = flag ? s : o;
		Object tmpO = temp;
	}
}