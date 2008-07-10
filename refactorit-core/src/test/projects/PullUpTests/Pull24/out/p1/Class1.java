package p1;

public class Class1 {
	private int t1;
//chain of uses
	private int tmp1;

	public void f1() {
	}
	public void f2() {
	}

	private int f3() {
		return 1;
	}

	public void func1() {
		tmp1 = func2();
	}

	public void func2() {
	}
}

class Class2 extends Class1 {

}
