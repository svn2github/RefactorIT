public class A {
	void test(boolean flag) {
		final byte b = 0;
		byte temp = (flag ? 5 : b);
		byte tmpB = temp;
	}
}