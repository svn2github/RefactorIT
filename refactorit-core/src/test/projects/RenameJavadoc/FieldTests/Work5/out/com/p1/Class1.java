package com.p1;

/**
* @see #tmp1_ren
*/

public class Class1 {
	public int tmp1_ren = 12; 
	public int tmp2 = 10;
	public int f() { return 11; }

	/**
	* @see #tmp1_ren
	*/



	public void method1() { }

        /**  @see #tmp1_ren */
        public void method2() { }
        /** @see #tmp1_ren */
        public int tmp3;
        /** @see #tmp1_ren */
        Class1() { }

	/**  @see #tmp1 */

	/** @see #tmp1 */

	public class Inner1 {
		public int tmp1 = 10; 
		
		/** @see #tmp1 */

		public class Inner2 {
			public int tmp1 = 10;

			/** @see #tmp1 */
			public class Inner3 {
				
			}
		}
	}


	/** @see #tmp1 */
	
	public class Inner1_1 {
		public int tmp1;
	}

        /**  @see #tmp1_ren */
        public void method3() { }
}

/**
* @see #tmp1
*/
class Class1_1 {
	public int tmp1;
}


