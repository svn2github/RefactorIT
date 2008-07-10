package p1;

public class A {
// move into X
// native has target instance and access modifier of f1 is strong
	public void f1() {
	}

	public void f2() {
		p2.X refX = new p2.X();
		f1();
	}
}
