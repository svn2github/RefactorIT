package p1;

class Super {
	void f2() {
	}
}


public class A extends Super {
// Move into X
// change access of f2
	public void f1() {
		f2();
	}
}
