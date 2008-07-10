package com.p2;

import com.p1.Class1;

/**
 * some text {@link #tmp1} some text {@link #method1}
 * @see #tmp1
 * @see #method1
 * @see com.p1.Class1#tmp1
 * @see com.p1.Class1#method2
 * @see Class1#tmp1
 * @see Class1#method2
 * @see Class1.Inner#tmp1
 * @see Class1.Inner#inner()
 */
public class Class3 extends Class2 {

	Class3() { }


/**
 * some text {@link Class3#tmp1} 
 * some text {@link Class3#method1}
 * @see com.p2.Class2#tmp1
 * @see com.p2.Class2#method1
 */ 
	public void f() {
	}

}
