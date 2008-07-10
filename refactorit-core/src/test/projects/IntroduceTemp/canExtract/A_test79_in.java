public class A {
	void test(boolean flag) {
		final byte b = 0;
		byte tmpB = (flag ? 5 : b);
	}
}