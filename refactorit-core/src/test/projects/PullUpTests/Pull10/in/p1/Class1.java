package p1;

public class Class1 {
	public int t1;
	public int t2;

	public void f1() {
	}
	public void f2() {
	}

	private int f3() {
		return 1;
	}
}

class Class2 extends Class1 {
//field uses method
	public int tmp3 = aaa();

	public void aaa() {
	}
}
