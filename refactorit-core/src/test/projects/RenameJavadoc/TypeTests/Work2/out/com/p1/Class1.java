package com.p1;

/**
  * @see Class1#tmp1
  * @see com.p1.Class1.Inner_ren#tmp1
*/
public class Class1 {
	public int tmp1 = 10;

	public void method1(int a1, int a2, int a3) {
		int a = 1;
	}
  
	public void method2() { 
	}

	public class Inner_ren {
		public int tmp1 = 11;
	
		Inner_ren() { }

		public void inner() {
		}
	}
}
