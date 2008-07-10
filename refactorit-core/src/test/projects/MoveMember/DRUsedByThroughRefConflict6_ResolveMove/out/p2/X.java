package p2;

public class X {

// Move into X
// f2 has no target instance so two choices are possible
// 1. move f3 also
// 2. make f1 static
// choose 1
	public void f1() {
	}

	public void f2() {
		f1();
	}
}
