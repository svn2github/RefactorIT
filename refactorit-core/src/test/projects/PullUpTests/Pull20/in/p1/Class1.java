package p1;
import p2.Class10;

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

class Class2 extends Class10 {
/* pull up of func1 but func2 stays since after pull up
   it will be able to see func1 because func1's access modifier
   will change */

	final
	void 
	func1() 
	{
	}

	public void func2() {
		func1();
	}
}
