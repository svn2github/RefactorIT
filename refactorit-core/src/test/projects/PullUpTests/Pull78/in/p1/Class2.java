package p1;
import p2.Class10;

public class Class2 extends Class10 {
	Class2() {
		func2();
	}

// old type reference
	public void func1(Class2 ref) {
		ref.func2();
	}

	private void func2() { }
}
