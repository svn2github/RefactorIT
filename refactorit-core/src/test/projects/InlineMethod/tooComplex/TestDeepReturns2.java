package tooComplex;

public class DeepReturns {
	public int foo() {
		return /*[*/bar()/*]*/;
	}
	
	public int bar() {
		if (true) {
			return 1;
		} else {
			return 2;
		}
	}
}