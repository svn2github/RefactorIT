package com.p1;

public class Class1 {
	public Class1() { }

	public int tmp1_1 = 1;
	protected int tmp2_1 = 2;
	int tmp3_1 = 3;
	static int tmp4_1 = 4;
	private int tmp5_1 = 5;

	public int tmp1_2 = 1;
	protected int tmp2_2 = 2;
	int tmp3_2 = 3;
	static int tmp4_2 = 4;
	private int tmp5_2 = 5;

	public int tmp1_3 = 1;
	protected int tmp2_3 = 2;
	int tmp3_3 = 3;
	static int tmp4_3 = 4;
	private int tmp5_3 = 5;

	public int tmp1_4 = 1;
	protected int tmp2_4 = 2;
	int tmp3_4 = 3;
	static int tmp4_4 = 4;
	private int tmp5_4 = 5;

	public int tmp1_5 = 1;
	protected int tmp2_5 = 2;
	int tmp3_5 = 3;
	static int tmp4_5 = 4;
	private int tmp5_5 = 5;

	public int tmp1_6 = 1;
	protected int tmp2_6 = 2;

	public int tmp1_7 = 1;

	public void f1() {
		tmp1_1 = 1;
		tmp2_1 = 2;
		tmp3_1 = 3;
		tmp4_1 = 4;
		tmp5_1 = 5;
	}

	public void foo1_1() { }
	protected void foo2_1() { }
	void foo3_1() { }

	public void foo1_2() { }
	protected void foo2_2() { }
	void foo3_2() { }
}

class Class2 {
        public void f() {
                new Class1().tmp1_2 = 1;
                new Class1().tmp2_2 = 2;
                new Class1().tmp3_2 = 3;
                new Class1().tmp4_2 = 4;
        }
}

class Class3 extends Class1 {
        public void f2() {
                tmp1_3 = 1;
        }

}

class Class4 extends Class3 {
        public void f3() {
                tmp2_3 = 2;
                tmp3_3 = 3;
                Class1.tmp4_3 = 4;
        }
}

class Class5 extends Class1 {
	public void foo1_1() { }
	public void foo2_1() { }
	public void foo3_1() { }

	public void foo1_2() { }
	public void foo2_2() { }
	public void foo3_2() { }

	private void foo() {
		foo1_1();
		foo2_1();
		foo3_1();
	}

}

class Class6 extends Class5 {
	private void foo() {
		foo1_2();
		foo2_2();
		foo3_2();
	}
}
