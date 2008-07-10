public class TestIncDec {
	public int foo(boolean b, int a) {
		int a1 = a;
		a1 = a1 - 1;
		return b ? a++ : (a1);
	}
	
	public int bar(int a) {
		a = a - 1;
		return a;
	}
}