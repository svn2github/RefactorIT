package com.p1;

     /** {@link #method2} some text
	* some text {@link #tmp1} some text
		* @see #method2
		* @see #tmp1
	*/
public class Class1 {
	public int tmp1 = 10;

/** @param a1 description
 * @param a2 description
 * @param a3 description
 */
	public void method1(int a1, int a2, int a3) {
		int a = 1;
	}
  
  /**
   * some text {@link Class1#tmp1} some text
   * some text {@link Class1#method2}
   * @see com.p1.Class1#tmp1
   * @see com.p1.Class1#method2
   */

/**
  * @throws Class1
  * @exception Class1
  * @throws Class1.Inner
  * @see Class1.Inner#tmp1
  * @see com.p1.Class1.Inner#f
*/
	public void method2() { 
	}

	class Inner {
		public int tmp1 = 11;
	
		public void f() {
		}
	
        	/**
	          * some text {@link #tmp1}
        	  * @see Class1#tmp1
	          * @see com.p1.Class1#tmp1
		  * @see Class1.Inner#tmp1
		  * @see com.p1.Class1.Inner#tmp1
        	*/
  	}
}

interface Inter1 {
        /**
        */
        class A { }

        void f1();
}

