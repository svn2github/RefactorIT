package p2;

public class Class10 {
// old type reference, double resolve 2: move, double resolve 1: change access
	public void func1(Class10 ref) {
		ref.func2();
	}

	protected void func2() { }
}
