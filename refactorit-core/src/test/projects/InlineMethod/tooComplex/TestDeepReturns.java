package tooComplex;

public class DeepReturns {
	public void foo() {
		int a = /*[*/bar()/*]*/;
	}
	
	public int bar() {
		if (true) {
			return 1;
		} 
		return 2;
	}
}