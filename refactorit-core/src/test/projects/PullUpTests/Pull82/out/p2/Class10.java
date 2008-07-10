package p2;

public class Class10 {
// old type reference, double resolve 2: move
	public void func1(Class10 ref) {
		ref.func2();
	}

	private void func2() { }
}
