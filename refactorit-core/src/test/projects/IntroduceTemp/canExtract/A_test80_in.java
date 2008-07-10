public class A {
	void test(boolean flag) {
		final byte b = 0;
		final short s = 0;
		short tmpS = (flag ? s : b);
	}
}