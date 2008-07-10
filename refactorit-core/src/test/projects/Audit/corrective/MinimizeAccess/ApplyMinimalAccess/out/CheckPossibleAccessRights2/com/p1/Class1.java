package CheckPossibleAccessRights2.com.p1;

/**
 * @violations 19
 */
public class Class1 {
	void foo1_1() { }
	void foo2_1() { }
	void foo3_1() { }

	void foo1_2() { }
	void foo2_2() { }
	void foo3_2() { }

	void foo1_3() { }
	void foo2_3() { }
	void foo3_3() { }

	void foo1_4() { }
	void foo2_4() { }
	void foo3_4() { }

	void foo1_5() { }
	void foo2_5() { }
	void foo3_5() { }
}

class Class2 extends Class1 {
	void foo1_1() { }
	void foo2_1() { }
	void foo3_1() { }

	void foo1_2() { }
	void foo2_2() { }
	void foo3_2() { }

	void foo1_3() { }
	void foo2_3() { }
	void foo3_3() { }

	private void foo() {
		foo1_1();
		foo2_1();
		foo3_1();
	}

}

class Class3 extends Class2 {
	private void foo() {
		foo1_2();
		foo2_2();
		foo3_2();
	}
}
