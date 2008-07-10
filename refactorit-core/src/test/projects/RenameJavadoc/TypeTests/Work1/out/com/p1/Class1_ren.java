package com.p1;

     /** {@link #method2} some text
	* some text {@link #tmp1} some text
		* @see #method2
		* @see #tmp1
	*/
public class Class1_ren {
	public int tmp1 = 10;

/** @param a1 description
 * @param a2 description
 * @param a3 description
 */
	public void method1(int a1, int a2, int a3) {
		int a = 1;
	}
  
  /**
   * some text {@link Class1_ren#tmp1} some text
   * some text {@link Class1_ren#method2}
   * @see com.p1.Class1_ren#tmp1
   * @see com.p1.Class1_ren#method2
   */

/**
  * @throws Class1_ren
  * @exception Class1_ren
  * @throws Class1_ren.Inner
  * @see Class1_ren.Inner#tmp1
  * @see com.p1.Class1_ren.Inner#f
*/
	public void method2() { 
	}

	class Inner {
		public int tmp1 = 11;
	
		public void f() {
		}
	
        	/**
	          * some text {@link #tmp1}
        	  * @see Class1_ren#tmp1
	          * @see com.p1.Class1_ren#tmp1
		  * @see Class1_ren.Inner#tmp1
		  * @see com.p1.Class1_ren.Inner#tmp1
        	*/
  	}
}

interface Inter1 {
        /**
        */
        class A { }

        void f1();
}

