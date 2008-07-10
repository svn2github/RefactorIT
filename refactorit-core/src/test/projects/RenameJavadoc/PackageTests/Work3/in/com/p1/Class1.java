package com.p1;

import java.lang.String;

/**
  * @see com.p1
  * @see com.p1.Class1
  * @see com.p1.p3
  * @see com.p1.p3.Class4
  * @see Class1#tmp1
*/
public class Class1 {
	public int tmp1;


	public void f1() {
		com.p1.Class1.tmp1 = 10;
		com.p1.p3.Class4.tmp99 = 11;
	}

}
