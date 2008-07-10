package p2;

public class X {

// Move into X:
// 1. change access of f1
// 2. move f2 also
// 3. make f1 static
// 2 is not possible since f2 is called by B.f, choose 1
	public void f1() {
	}
}
