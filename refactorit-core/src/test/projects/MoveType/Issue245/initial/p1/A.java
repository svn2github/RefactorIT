package p1;

public class A {
	static class Inner3 {
	}
	
	static interface Inner1 {
	}

	public static class Inner2 implements Inner1 {
		Inner3 inner3;
	}
}