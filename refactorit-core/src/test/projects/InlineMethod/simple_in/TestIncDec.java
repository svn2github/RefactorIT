public class TestIncDec {
	public int foo(boolean b, int a) {
		return b ? a++ : /*[*/bar(a)/*]*/;
	}
	
	public int bar(int a) {
		a = a - 1;
		return a;
	}
}