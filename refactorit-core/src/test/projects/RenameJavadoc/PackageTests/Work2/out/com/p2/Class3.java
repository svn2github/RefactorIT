package com.p2;

/**
  * @see com.p1.Class1#f1
  * @see com.p1.p3_ren.Class4#tmp99
*/
public class Class3 extends Class2 {

	Class3() { }

	public void f() {
		com.p1.Class1.f1();
		com.p1.p3_ren.Class4.tmp99 = 11;
	}	
}
