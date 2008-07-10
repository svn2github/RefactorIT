public class A {
	void test(boolean flag) {
		final byte b = 0;
		final short s = 0;
		short temp = (flag ? s : b);
		short tmpS = temp;
	}
}