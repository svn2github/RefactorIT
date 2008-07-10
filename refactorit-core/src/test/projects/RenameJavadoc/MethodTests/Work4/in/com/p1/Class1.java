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
	public void method2() { 
	}

	/**
	  * @see #method9
	  * @see #method10(int)
	  * @see #method11(float)
	  * @see #method12(int, char)
	  * @see #method13(Class1,	 String)
	*/
	public void method9() { }
	public void method10(int a) { }
	public void method11(float a) { }
	public void method12(int a, char b) { }
	public void method13(Class1 a, String b) { }
}
