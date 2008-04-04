/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader.jdk5;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AutoBoxingTest extends AbstractComparisonTest {

	public AutoBoxingTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 65 };
//		TESTS_RANGE = new int[] { 11, -1 };
//	}
	public static Test suite() {
    return new TestSuite(AutoBoxingTest.class);
	}

	public static Class testClass() {
		return AutoBoxingTest.class;
	}

	public void test001() { // constant cases of base type -> Number
		// int -> Integer
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(1);\n" +
				"	}\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// byte -> Byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test((byte)127);\n" +
				"	}\n" +
				"	public static void test(Byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// char -> Character
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test('b');\n" +
				"	}\n" +
				"	public static void test(Character c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// float -> Float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(-0.0f);\n" +
				"	}\n" +
				"	public static void test(Float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// double -> Double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(0.0);\n" +
				"	}\n" +
				"	public static void test(Double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// long -> Long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(Long.MAX_VALUE);\n" +
				"	}\n" +
				"	public static void test(Long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// short -> Short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(Short.MAX_VALUE);\n" +
				"	}\n" +
				"	public static void test(Short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// boolean -> Boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(false);\n" +
				"	}\n" +
				"	public static void test(Boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test002() { // non constant cases of base type -> Number
		// int -> Integer
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static int bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// byte -> Byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static byte bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// char -> Character
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static char bar() {return 'c';}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Character c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// float -> Float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static float bar() {return 0.0f;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// double -> Double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static double bar() {return 0.0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// long -> Long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static long bar() {return 0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// short -> Short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static short bar() {return 0;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// boolean -> Boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static boolean bar() {return true;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(bar());\n" +
				"	}\n" +
				"	public static void test(Boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test003() { // Number -> base type
		// Integer -> int
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Integer(1));\n" +
				"	}\n" +
				"	public static void test(int i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Byte -> byte
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Byte((byte) 1));\n" +
				"	}\n" +
				"	public static void test(byte b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Byte -> long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Byte((byte) 1));\n" +
				"	}\n" +
				"	public static void test(long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Character -> char
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Character('c'));\n" +
				"	}\n" +
				"	public static void test(char c) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Float -> float
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Float(0.0f));\n" +
				"	}\n" +
				"	public static void test(float f) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Double -> double
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Double(0.0));\n" +
				"	}\n" +
				"	public static void test(double d) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Long -> long
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Long(0L));\n" +
				"	}\n" +
				"	public static void test(long l) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Short -> short
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(new Short((short) 0));\n" +
				"	}\n" +
				"	public static void test(short s) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		// Boolean -> boolean
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(Boolean.TRUE);\n" +
				"	}\n" +
				"	public static void test(boolean b) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test004() { // autoboxing method is chosen over private exact match & visible varargs method
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private static void test(int i) { System.out.print('n'); }\n" +
				"	static void test(int... i) { System.out.print('n'); }\n" +
				"	public static void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	private void test(int i) { System.out.print('n'); }\n" +
				"	void test(int... i) { System.out.print('n'); }\n" +
				"	public void test(Integer i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test005() { // this is NOT an ambiguous case as 'long' is matched before autoboxing kicks in
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i) { System.out.print('n'); }\n" +
				"	void test(long i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test006() {
		this.runNegativeTest( // Integers are not compatible with Longs, even though ints are compatible with longs
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Long i, int j) { System.out.print('n'); }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\r\n" +
			"	new Y().test(1, 1);\r\n" +
			"	        ^^^^\n" +
			"The method test(Long, int) in the type Y is not applicable for the arguments (int, int)\n" +
			"----------\n"
			// test(java.lang.Long,int) in Y cannot be applied to (int,int)
		);
		this.runNegativeTest( // likewise with Byte and Integer
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test((byte) 1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) { System.out.print('n'); }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\r\n" +
			"	new Y().test((byte) 1, 1);\r\n" +
			"	        ^^^^\n" +
			"The method test(Integer, int) in the type Y is not applicable for the arguments (byte, int)\n" +
			"----------\n"
			// test(java.lang.Integer,int) in Y cannot be applied to (byte,int)
		);
	}

	public void test007() {
		this.runConformTest( // this is NOT an ambiguous case as Long is not a match for int
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Long i, int j) { System.out.print('n'); }\n" +
				"	void test(long i, Integer j) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test008() { // test autoboxing AND varargs method match
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(1, new Integer(2), -3);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void test(int ... i) { System.out.print('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test009() {
		this.runNegativeTest( // 2 of these sends are ambiguous
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(1, 1);\n" + // reference to test is ambiguous, both method test(java.lang.Integer,int) in Y and method test(int,java.lang.Integer) in Y match
				"		new Y().test(new Integer(1), new Integer(1));\n" + // reference to test is ambiguous
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) {}\n" +
				"	void test(int i, Integer j) {}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\r\n" +
			"	new Y().test(1, 1);\r\n" +
			"	        ^^^^\n" +
			"The method test(Integer, int) is ambiguous for the type Y\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\r\n" +
			"	new Y().test(new Integer(1), new Integer(1));\r\n" +
			"	        ^^^^\n" +
			"The method test(Integer, int) is ambiguous for the type Y\n" +
			"----------\n"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(new Integer(1), 1);\n" +
				"		new Y().test(1, new Integer(1));\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) { System.out.print(1); }\n" +
				"	void test(int i, Integer j) { System.out.print(2); }\n" +
				"}\n",
			},
			"12"
		);
	}

	public void test010() { // local declaration assignment tests
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		int i = Y.test();\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static Byte test() { return new Byte((byte) 1); }\n" +
				"}\n",
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Object o = Y.test();\n" +
				"		System.out.print(o);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int test() { return 1; }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test011() { // field declaration assignment tests
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static int i = Y.test();\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static Byte test() { return new Byte((byte) 1); }\n" +
				"}\n",
			},
			"1"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static Object o = Y.test();\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.print(o);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static int test() { return 1; }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test012() { // varargs and autoboxing
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer x = new Integer(15); \n" +
				"		int y = 32;\n" +
				"		System.out.printf(\"%x + %x\", x, y);\n" +
				"	}\n" +
				"}",
			},
			"f + 20"
		);
	}

	public void test013() { // foreach and autoboxing
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		int[] tab = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
				"		for (final Integer e : tab) {\n" +
				"			System.out.print(e);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"123456789"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer[] tab = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
				"		for (final int e : tab) {\n" +
				"			System.out.print(e);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"123456789"
		);
	}

	public void test014() { // switch
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Integer i = new Integer(1);\n" +
				"		switch(i) {\n" +
				"			case 1 : System.out.print('y');\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test015() { // return statement
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static Integer foo1() {\n" +
				"		return 0;\n" +
				"	}\n" +
				"	static int foo2() {\n" +
				"		return new Integer(0);\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(foo1());\n" +
				"		System.out.println(foo2());\n" +
				"	}\n" +
				"}\n",
			},
			"00"
		);
	}

	public void test016() { // conditional expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = args.length == 0 ? 0 : new Integer(1);\n" +
				"		System.out.println(i);\n" +
				"	}\n" +
				"}\n",
			},
			"0"
		);
	}

	public void test017() { // cast expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = new Integer(1);\n" +
				"		System.out.println((int)i);\n" +
				"	}\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test018() { // cast expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Float f = args.length == 0 ? new Float(0) : 0;\n" +
				"		System.out.println((int)f);\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Float f = args.length == 0 ? new Float(0) : 0;\n" +
			"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The expression of type float is boxed into Float\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 3)\n" +
			"	Float f = args.length == 0 ? new Float(0) : 0;\n" +
			"	                             ^^^^^^^^^^^^\n" +
			"The expression of type Float is unboxed into float\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	System.out.println((int)f);\n" +
			"	                   ^^^^^^\n" +
			"Cannot cast from Float to int\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 4)\n" +
			"	System.out.println((int)f);\n" +
			"	                        ^\n" +
			"The expression of type Float is unboxed into int\n" +
			"----------\n"
		);
	}

	public void test019() { // cast expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println((Integer) 0);\n" +
				"		System.out.println((Float) 0);\n" +
				"		\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	System.out.println((Integer) 0);\n" +
			"	                             ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	System.out.println((Float) 0);\n" +
			"	                   ^^^^^^^^^\n" +
			"Cannot cast from int to Float\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 4)\n" +
			"	System.out.println((Float) 0);\n" +
			"	                           ^\n" +
			"The expression of type int is boxed into Float\n" +
			"----------\n"
		);
	}

	public void test020() { // binary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"      System.out.println(2 + b);\n" +
				"    }\n" +
				"}\n",
			},
			"3"
		);
	}

	public void test021() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    Integer i = +b + (-b);\n" +
				"		System.out.println(i);\n" +
				"    }\n" +
				"}\n",
			},
			"0"
		);
	}

	public void test022() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    Integer i = 0;\n" +
				"	    int n = b + i;\n" +
				"		System.out.println(n);\n" +
				"    }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test023() { // 78849
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Character cValue = new Character('c');\n" +
				"		if ('c' == cValue) System.out.println('y');\n" +
				"	}\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test024() { // 79254
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) { test(2); }\n" +
				"	static void test(Object o) { System.out.println('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test025() { // 79641
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) { test(true); }\n" +
				"	static void test(Object ... o) { System.out.println('y'); }\n" +
				"}\n",
			},
			"y"
		);
	}

	public void test026() { // compound assignment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    Integer i = 0;\n" +
				"	    i += b;\n" +
				"		System.out.println(i);\n" +
				"    }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test027() { // equal expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		if (0 == new X()) {\n" +
				"			System.out.println();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	if (0 == new X()) {\n" +
			"	    ^^^^^^^^^^^^\n" +
			"Incompatible operand types int and X\n" +
			"----------\n"
		);
	}

	public void test028() { // unary expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    int i = +b;\n" +
				"		System.out.println(i);\n" +
				"    }\n" +
				"}\n",
			},
			"1"
		);
	}

	public void test029() { // generic type case
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	sum += iterator.next();\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}

	public void test030() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		Boolean b = Boolean.TRUE;\n" +
				"		\n" +
				"		if (b && !b) {\n" +
				"			System.out.print(\"THEN\");\n" +
				"		} else {\n" +
				"			System.out.print(\"ELSE\");\n" +
				"		}\n" +
				"    }\n" +
				"}",
			},
			"ELSE"
		);
	}

	public void test031() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Boolean foo() { return Boolean.FALSE; }\n" +
				"	public static void main(String[] args) {\n" +
				"		Boolean b = foo();\n" +
				"		\n" +
				"		if (!b) {\n" +
				"			System.out.print(\"THEN\");\n" +
				"		} else {\n" +
				"			System.out.print(\"ELSE\");\n" +
				"		}\n" +
				"    }\n" +
				"}",
			},
			"THEN"
		);
	}

	public void test032() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public static void main(String[] s) {\n" +
				"      if (new Integer(1) == new Integer(0)) {\n" +
				"         System.out.println();\n" +
				"      }\n" +
				"      System.out.print(\"SUCCESS\");\n" +
				"   }\n" +
				"}",
			},
			"SUCCESS"
		);

		/*ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}

		String expectedOutput =
			"  // Method descriptor  #15 ([Ljava/lang/String;)V\n" +
			"  // Stack: 4, Locals: 1\n" +
			"  public static void main(String[] s);\n" +
			"     0  new java/lang/Integer [17]\n" +
			"     3  dup\n" +
			"     4  iconst_1\n" +
			"     5  invokespecial java/lang/Integer.<init>(I)V [20]\n" +
			"     8  new java/lang/Integer [17]\n" +
			"    11  dup\n" +
			"    12  iconst_0\n" +
			"    13  invokespecial java/lang/Integer.<init>(I)V [20]\n" +
			"    16  if_acmpne 25\n" +
			"    19  getstatic java/lang/System.out Ljava/io/PrintStream; [26]\n" +
			"    22  invokevirtual java/io/PrintStream.println()V [31]\n" +
			"    25  getstatic java/lang/System.out Ljava/io/PrintStream; [26]\n" +
			"    28  ldc <String \"SUCCESS\"> [33]\n" +
			"    30  invokevirtual java/io/PrintStream.print(Ljava/lang/String;)V [37]\n" +
			"    33  return\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}*/
	}

	public void test033() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"   public static void main(String[] s) {\n" +
				"      System.out.print(Boolean.TRUE || Boolean.FALSE);\n" +
				"   }\n" +
				"}",
			},
			"true"
		);
	}

	public void test034() { // postfix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    int i = b++;\n" +
				"		System.out.print(i);\n" +
				"		System.out.print(b);\n" +
				"    }\n" +
				"}\n",
			},
			"12"
		);
	}

	public void test035() { // postfix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    int i = b--;\n" +
				"		System.out.print(i);\n" +
				"		System.out.print(b);\n" +
				"    }\n" +
				"}\n",
			},
			"10"
		);
	}

	public void test036() { // prefix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    int i = ++b;\n" +
				"		System.out.print(i);\n" +
				"		System.out.print(b);\n" +
				"    }\n" +
				"}\n",
			},
			"22"
		);
	}

	public void test037() { // prefix expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"	    Byte b = new Byte((byte)1);\n" +
				"	    int i = --b;\n" +
				"		System.out.print(i);\n" +
				"		System.out.print(b);\n" +
				"    }\n" +
				"}\n",
			},
			"00"
		);
	}

	public void test038() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static boolean foo() { return false; }\n" +
				"   public static void main(String[] s) {\n" +
				"		boolean b = foo();\n" +
				"      System.out.print(b || Boolean.FALSE);\n" +
				"   }\n" +
				"}",
			},
			"false"
		);
	}

	public void test039() { // equal expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		int i = 0;\n" +
				"		if (i != null) {\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if (i != null) {\n" +
			"	    ^^^^^^^^^\n" +
			"The operator != is undefined for the argument type(s) int, null\n" +
			"----------\n"
		);
	}

	public void test040() { // boolean expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = new Integer(1);\n" +
				"		if (i == null)\n" +
				"			i++;\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}",
			},
			"1"
		);
	}

	public void test041() { // equal expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = 0;\n" +
				"		if (i != null) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS"
		);
	}

	public void test042() { // conditional expression
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Boolean bar() { return Boolean.TRUE; } \n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = bar() ? new Integer(1) : null;\n" +
				"		int j = i;\n" +
				"		System.out.print(j);\n" +
				"	}\n" +
				"}",
			},
			"1"
		);
	}

	public void test043() { // compound assignment
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = 0;\n" +
				"		i += \"aaa\";\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Integer i = 0;\n" +
			"	            ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	i += \"aaa\";\n" +
			"	^^^^^^^^^^\n" +
			"The operator += is undefined for the argument type(s) Integer, String\n" +
			"----------\n");
	}

	public void test044() { // compound assignment
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = 0;\n" +
				"		i += null;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Integer i = 0;\n" +
			"	            ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	i += null;\n" +
			"	^^^^^^^^^\n" +
			"The operator += is undefined for the argument type(s) Integer, null\n" +
			"----------\n");
	}

	public void test045() { // binary expression
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer i = 0;\n" +
				"		i = i + null;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Integer i = 0;\n" +
			"	            ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	i = i + null;\n" +
			"	    ^^^^^^^^\n" +
			"The operator + is undefined for the argument type(s) Integer, null\n" +
			"----------\n");
	}

	public void test046() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Byte b = new Byte((byte)1);\n" +
				"		b++;\n" +
				"		System.out.println((Byte)b);\n" +
				"	}\n" +
				"}\n",
			},
			"2");
	}

	public void test047() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Byte b = new Byte((byte)1);\n" +
				"		b++;\n" +
				"		if (b instanceof Byte) {\n" +
				"			System.out.println(\"SUCCESS\" + b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS2");
	}

	public void test048() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Byte b = new Byte((byte)1);\n" +
				"	public static void main(String[] s) {\n" +
				"		b++;\n" +
				"		if (b instanceof Byte) {\n" +
				"			System.out.print(\"SUCCESS\" + b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS2");
	}

	public void test049() { // postfix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static class Y {\n" +
				"		public static Byte b = new Byte((byte)1);\n" +
				"	}\n" +
				"	public static void main(String[] s) {\n" +
				"		X.Y.b++;\n" +
				"		if (X.Y.b instanceof Byte) {\n" +
				"			System.out.print(\"SUCCESS\" + X.Y.b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS2");
	}

	public void test050() { // prefix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static Byte b = new Byte((byte)1);\n" +
				"	public static void main(String[] s) {\n" +
				"		++b;\n" +
				"		if (b instanceof Byte) {\n" +
				"			System.out.print(\"SUCCESS\" + b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS2");
	}

	public void test051() { // prefix increment
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static class Y {\n" +
				"		public static Byte b = new Byte((byte)1);\n" +
				"	}\n" +
				"	public static void main(String[] s) {\n" +
				"		++X.Y.b;\n" +
				"		if (X.Y.b instanceof Byte) {\n" +
				"			System.out.print(\"SUCCESS\" + X.Y.b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS2");
	}

	public void test052() { // boxing in var decl
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Byte b = 0;\n" +
				"		++b;\n" +
				"		foo(0);\n" +
				"	}\n" +
				"	static void foo(Byte b) {\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Byte b = 0;\n" +
			"	         ^\n" +
			"The expression of type int is boxed into Byte\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	++b;\n" +
			"	^^^\n" +
			"The expression of type byte is boxed into Byte\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 4)\n" +
			"	++b;\n" +
			"	  ^\n" +
			"The expression of type Byte is unboxed into byte\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 5)\n" +
			"	foo(0);\n" +
			"	^^^\n" +
			"The method foo(Byte) in the type X is not applicable for the arguments (int)\n" +
			"----------\n");
	}

	public void test053() { // boxing in var decl
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Byte b = 1;\n" +
				"		++b;\n" +
				"		if (b instanceof Byte) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test054() { // boxing in field decl
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static Byte b = 1;\n" +
				"	public static void main(String[] s) {\n" +
				"		++b;\n" +
				"		if (b instanceof Byte) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test055() { // boxing in foreach
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		byte[] bytes = {0, 1, 2};\n" +
				"		for(Integer i : bytes) {\n" +
				"			System.out.print(i);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	for(Integer i : bytes) {\n" +
			"	                ^^^^^\n" +
			"Type mismatch: cannot convert from element type byte to Integer\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	for(Integer i : bytes) {\n" +
			"	                ^^^^^\n" +
			"The expression of type byte is boxed into Integer\n" +
			"----------\n");
	}

	public void test056() { // boxing in foreach
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		int[] ints = {0, 1, 2};\n" +
				"		for(Integer i : ints) {\n" +
				"			System.out.print(i);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"012");
	}

	public void test057() { // boxing in foreach
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		byte[] bytes = {0, 1, 2};\n" +
				"		for(Byte b : bytes) {\n" +
				"			System.out.print(b);\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"012");
	}

	public void test058() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (Integer i : list) {\n" +
				"	    	sum += i;\n" +
				"	    }	    \n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}\n",
			},
			"10");
	}

	public void test059() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	if (1 == iterator.next()) {\n" +
				"	    		System.out.println(\"SUCCESS\");\n" +
				"	    		break;\n" +
				"	    	}\n" +
				"	    }\n" +
				"    }\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test060() { // autoboxing and boolean expr
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Boolean> list = new ArrayList<Boolean>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i % 2 == 0);\n" +
				"	    }\n" +
				"	    for (Iterator<Boolean> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	if (iterator.next()) {\n" +
				"	    		System.out.println(\"SUCCESS\");\n" +
				"	    		break;\n" +
				"	    	}\n" +
				"	    }\n" +
				"    }\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test061() { // autoboxing and boolean expr
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Boolean> list = new ArrayList<Boolean>();\n" +
				"		boolean b = true;\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add((i % 2 == 0) && b);\n" +
				"	    }\n" +
				"	    for (Iterator<Boolean> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	if (iterator.next()) {\n" +
				"	    		System.out.println(\"SUCCESS\");\n" +
				"	    		break;\n" +
				"	    	}\n" +
				"	    }\n" +
				"    }\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test062() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		boolean b = true;\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"		int sum = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	sum = sum + iterator.next();\n" +
				"	    }\n" +
				"	    System.out.println(sum);\n" +
				"    }\n" +
				"}\n",
			},
			"10");
	}

	public void test063() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		boolean b = true;\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"		int val = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	val = ~ iterator.next();\n" +
				"	    }\n" +
				"	    System.out.println(val);\n" +
				"    }\n" +
				"}\n",
			},
			"-5");
	}

	public void test064() { // autoboxing and generics
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		boolean b = true;\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"		int val = 0;\n" +
				"	    for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {\n" +
				"	    	val += (int) iterator.next();\n" +
				"	    }\n" +
				"	    System.out.println(val);\n" +
				"    }\n" +
				"}\n",
			},
			"10");
	}

	public void test065() { // generic type case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (int i : list) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}

	public void test066() { // array case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer[] tab = new Integer[] {0, 1, 2, 3, 4};\n" +
				"	    int sum = 0;\n" +
				"	    for (int i : tab) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}

	public void test067() { // array case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		int[] tab = new int[] {0, 1, 2, 3, 4};\n" +
				"	    int sum = 0;\n" +
				"	    for (Integer i : tab) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}

	public void test068() { // generic type case + foreach statement
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Integer> list = new ArrayList<Integer>();\n" +
				"		for (int i = 0; i < 5; i++) {\n" +
				"			list.add(i);\n" +
				"	    }\n" +
				"	    int sum = 0;\n" +
				"	    for (Integer i : list) {\n" +
				"	    	sum += i;\n" +
				"	    }\n" +
				"        System.out.print(sum);\n" +
				"    }\n" +
				"}",
			},
			"10"
		);
	}

	public void test069() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Boolean bool = true;\n" +
				"		assert bool : \"failed\";\n" +
				"	    System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test070() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Boolean> lb = new ArrayList<Boolean>();\n" +
				"		lb.add(true);\n" +
				"		Iterator<Boolean> iterator = lb.iterator();\n" +
				"		assert iterator.next() : \"failed\";\n" +
				"	    System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	public void test071() { // assert
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		List<Boolean> lb = new ArrayList<Boolean>();\n" +
				"		lb.add(true);\n" +
				"		Iterator<Boolean> iterator = lb.iterator();\n" +
				"		assert args != null : iterator.next();\n" +
				"	    System.out.println(\"SUCCESS\");\n" +
				"	}\n" +
				"}\n",
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81971
	public void test072() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        doFoo(getVoid());\n" +
				"    }\n" +
				"\n" +
				"    private static void doFoo(Object o) { }\n" +
				"\n" +
				"    private static void getVoid() { }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	doFoo(getVoid());\n" +
			"	^^^^^\n" +
			"The method doFoo(Object) in the type X is not applicable for the arguments (void)\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81571
	public void test073() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        a(new Integer(1), 2);\n" +
				"    }\n" +
				"    public static void a(int a, int b) { System.out.println(\"SUCCESS\"); }\n" +
				"    public static void a(Object a, Object b) {}\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\r\n" +
			"	a(new Integer(1), 2);\r\n" +
			"	^\n" +
			"The method a(int, int) is ambiguous for the type X\n" +
			"----------\n"
			// a is ambiguous, both method a(int,int) in X and method a(java.lang.Object,java.lang.Object) in X match
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432
	public void test074() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" Object e() {\n" +
				"  return \"\".compareTo(\"\") > 0;\n" +
				" }\n" +
				" public static void main(String[] args) {\n" +
				"  System.out.print(new X().e());\n" +
				" }\n" +
				"}",
			},
			"false");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432 - variation
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" Object e() {\n" +
				"  return \"\".compareTo(\"\") > 0;\n" +
				" }\n" +
				" public static void main(String[] args) {\n" +
				"  System.out.print(new X().e());\n" +
				" }\n" +
				" Zork z;\n" +
				"}",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	return \"\".compareTo(\"\") > 0;\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432 - variation
	public void test076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" Object e() {\n" +
				" int i = 12; \n" +
				"  boolean b = false;\n" +
				"  switch(i) {\n" +
				"    case 0: return i > 0;\n" +
				"    case 1: return i >= 0;\n" +
				"    case 2: return i < 0;\n" +
				"    case 3: return i <= 0;\n" +
				"    case 4: return i == 0;\n" +
				"    case 5: return i != 0;\n" +
				"    case 6: return i & 0;\n" +
				"    case 7: return i ^ 0;\n" +
				"    case 8: return i | 0;\n" +
				"    case 9: return b && b;\n" +
				"    default: return b || b;\n" +
				"  }\n" +
				" }\n" +
				" public static void main(String[] args) {\n" +
				"  System.out.print(new X().e());\n" +
				" }\n" +
				" Zork z;\n" +
				"}",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 6)\n" +
			"	case 0: return i > 0;\n" +
			"	               ^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 7)\n" +
			"	case 1: return i >= 0;\n" +
			"	               ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 8)\n" +
			"	case 2: return i < 0;\n" +
			"	               ^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 9)\n" +
			"	case 3: return i <= 0;\n" +
			"	               ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 10)\n" +
			"	case 4: return i == 0;\n" +
			"	               ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 11)\n" +
			"	case 5: return i != 0;\n" +
			"	               ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 12)\n" +
			"	case 6: return i & 0;\n" +
			"	               ^^^^^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 13)\n" +
			"	case 7: return i ^ 0;\n" +
			"	               ^^^^^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 14)\n" +
			"	case 8: return i | 0;\n" +
			"	               ^^^^^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"10. WARNING in X.java (at line 15)\n" +
			"	case 9: return b && b;\n" +
			"	               ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"11. WARNING in X.java (at line 16)\n" +
			"	default: return b || b;\n" +
			"	                ^^^^^^\n" +
			"The expression of type boolean is boxed into Boolean\n" +
			"----------\n" +
			"12. ERROR in X.java (at line 22)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82432 - variation
	public void test077() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				" Object e() {\n" +
				" int i = 12; \n" +
				"  boolean b = false;\n" +
				"  switch(i) {\n" +
				"    case 0: return i > 0;\n" +
				"    case 1: return i >= 0;\n" +
				"    case 2: return i < 0;\n" +
				"    case 3: return i <= 0;\n" +
				"    case 4: return i == 0;\n" +
				"    case 5: return i != 0;\n" +
				"    case 6: return i & 0;\n" +
				"    case 7: return i ^ 0;\n" +
				"    case 8: return i | 0;\n" +
				"    case 9: return b && b;\n" +
				"    default: return b || b;\n" +
				"  }\n" +
				" }\n" +
				" public static void main(String[] args) {\n" +
				"  System.out.print(new X().e());\n" +
				" }\n" +
				"}",
			},
			"false");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81923
	public void test078() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
				"	public <A extends T> X(A... t) {}\n" +
				"	<T> void foo(T... t) {}\n" +
				"	<T> void zip(T t) {}\n" +
				"	void test() {\n" +
				"		new X<Integer>(10, 20);\n" +
				"		foo(10);\n" +
				"		foo(10, 20);\n" +
				"		zip(10);\n" +
				"	}\n" +
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82407
	public void _test079() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"public class X {\n" +
				"	static HashMap<Character, Character> substitutionList(String s1, String s2) {\n" +
				"		HashMap<Character, Character> subst = new HashMap<Character, Character>();\n" +
				"		for (int i = 0; i < s1.length(); i++) {\n" +
				"			char key = s1.charAt(i);\n" +
				"			char value = s2.charAt(i);\n" +
				"			if (subst.containsKey(key)) {\n" +
				"				if (value != subst.get(key)) {\n" +
				"					return null;\n" +
				"				}\n" +
				"			} else if (subst.containsValue(value)) {\n" +
				"				return null;\n" +
				"			} else {\n" +
				"				subst.put(key, value);\n" +
				"			}\n" +
				"		}\n" +
				"		return subst;\n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"Bogon\");\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82407 - variation
	public void _test080() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		HashMap<Character, Character> subst = new HashMap<Character, Character>();\n" +
				"		subst.put(\'a\', \'a\');\n" +
				"		if (\'a\' == subst.get(\'a\')) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82407 - variation
	public void _test081() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		HashMap<Byte, Byte> subst = new HashMap<Byte, Byte>();\n" +
				"		subst.put((byte)1, (byte)1);\n" +
				"		if (1 + subst.get((byte)1) > 0.f) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}		\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82859
	public void test082() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String argv[]) {\n" +
				"		System.out.println(void.class == Void.TYPE);\n" +
				"	}\n" +
				"}"
			},
			"true"
		);
	}

		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82647
	public void test083() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	int counter = 0;\n" +
				"\n" +
				"	public boolean wasNull() {\n" +
				"		return ++counter % 2 == 0;\n" +
				"	}\n" +
				"\n" +
				"	private Byte getByte() {\n" +
				"		return (byte) 0;\n" +
				"	}\n" +
				"\n" +
				"	private Short getShort() {\n" +
				"		return (short) 0;\n" +
				"	}\n" +
				"\n" +
				"	private Long getLong() {\n" +
				"		return 0L;\n" +
				"	}\n" +
				"\n" +
				"	private Integer getInt() {\n" +
				"		return 0; // autoboxed okay\n" +
				"	}\n" +
				"\n" +
				"	// This should be the same as the second one.\n" +
				"	private Byte getBytey() {\n" +
				"		byte value = getByte();\n" +
				"		return wasNull() ? null : value;\n" +
				"	}\n" +
				"\n" +
				"	private Byte getByteyNoBoxing() {\n" +
				"		byte value = getByte();\n" +
				"		return wasNull() ? null : (Byte) value;\n" +
				"	}\n" +
				"\n" +
				"	// This should be the same as the second one.\n" +
				"	private Short getShorty() {\n" +
				"		short value = getShort();\n" +
				"		return wasNull() ? null : value;\n" +
				"	}\n" +
				"\n" +
				"	private Short getShortyNoBoxing() {\n" +
				"		short value = getShort();\n" +
				"		return wasNull() ? null : (Short) value;\n" +
				"	}\n" +
				"\n" +
				"	// This should be the same as the second one.\n" +
				"	private Long getLongy() {\n" +
				"		long value = getLong();\n" +
				"		return wasNull() ? null : value;\n" +
				"	}\n" +
				"\n" +
				"	private Long getLongyNoBoxing() {\n" +
				"		long value = getLong();\n" +
				"		return wasNull() ? null : (Long) value;\n" +
				"	}\n" +
				"\n" +
				"	// This should be the same as the second one.\n" +
				"	private Integer getIntegery() {\n" +
				"		int value = getInt();\n" +
				"		return wasNull() ? null : value;\n" +
				"	}\n" +
				"\n" +
				"	private Integer getIntegeryNoBoxing() {\n" +
				"		int value = getInt();\n" +
				"		return wasNull() ? null : (Integer) value;\n" +
				"	}\n" +
				"}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82647 - variation
	public void test084() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	Short foo() {\n" +
				"		short value = 0;\n" +
				"		return this == null ? null : value;\n" +
				"	}\n" +
				"	boolean bar() {\n" +
				"		short value = 0;\n" +
				"		return null == value;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	return this == null ? null : value;\n" +
			"	                             ^^^^^\n" +
			"The expression of type short is boxed into Short\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	return null == value;\n" +
			"	       ^^^^^^^^^^^^^\n" +
			"The operator == is undefined for the argument type(s) null, short\n" +
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83965
	public void test085() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	private static void checkByteConversions(Byte _byte) {\n" +
				"		short s = (short) _byte;\n" +
				"		short s2 = _byte;\n" +
				"		int i = (int) _byte;\n" +
				"		long l = (long) _byte;\n" +
				"		float f = (float) _byte;\n" +
				"		double d = (double) _byte;\n" +
				"		if ( _byte.byteValue() != s ) {\n" +
				"            System.err.println(\"Must be equal 0\");\n" +
				"        }\n" +
				"		if ( _byte.byteValue() != i ) {\n" +
				"            System.err.println(\"Must be equal 1\");\n" +
				"        }\n" +
				"		if ( _byte.byteValue() != l ) {\n" +
				"            System.err.println(\"Must be equal 2\");\n" +
				"        }\n" +
				"		if ( _byte.byteValue() != f ) {\n" +
				"            System.err.println(\"Must be equal 3\");\n" +
				"        }\n" +
				"		if ( _byte.byteValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 4\");\n" +
				"        }\n" +
				"	} \n" +
				"\n" +
				"	private static void checkCharacterConversions(Character _character) {\n" +
				"		int i = (int) _character;\n" +
				"		long l = (long) _character;\n" +
				"		float f = (float) _character;\n" +
				"		double d = (double) _character;\n" +
				"		if ( _character.charValue() != i ) {\n" +
				"            System.err.println(\"Must be equal 9\");\n" +
				"        }\n" +
				"		if ( _character.charValue() != l ) {\n" +
				"            System.err.println(\"Must be equal 10\");\n" +
				"        }\n" +
				"		if ( _character.charValue() != f ) {\n" +
				"            System.err.println(\"Must be equal 11\");\n" +
				"        }\n" +
				"		if ( _character.charValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 12\");\n" +
				"        }\n" +
				"	}\n" +
				"\n" +
				"	private static void checkFloatConversions(Float _float) {\n" +
				"		double d = (double) _float;\n" +
				"		if ( _float.floatValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 18\");\n" +
				"        }\n" +
				"	}\n" +
				"\n" +
				"	private static void checkIntegerConversions(Integer _integer) {\n" +
				"		long l = (long) _integer;\n" +
				"		float f = (float) _integer;\n" +
				"		double d = (double) _integer;\n" +
				"		if ( _integer.intValue() != l ) {\n" +
				"            System.err.println(\"Must be equal 13\");\n" +
				"        }\n" +
				"		if ( _integer.intValue() != f ) {\n" +
				"            System.err.println(\"Must be equal 14\");\n" +
				"        }\n" +
				"		if ( _integer.intValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 15\");\n" +
				"        }\n" +
				"	}\n" +
				"\n" +
				"	private static void checkIntegerConversions(Short _short) {\n" +
				"		int i = (int) _short;\n" +
				"		long l = (long) _short;\n" +
				"		float f = (float) _short;\n" +
				"		double d = (double) _short;\n" +
				"		if ( _short.shortValue() != i ) {\n" +
				"            System.err.println(\"Must be equal 5\");\n" +
				"        }\n" +
				"		if ( _short.shortValue() != l ) {\n" +
				"            System.err.println(\"Must be equal 6\");\n" +
				"        }\n" +
				"		if ( _short.shortValue() != f ) {\n" +
				"            System.err.println(\"Must be equal 7\");\n" +
				"        }\n" +
				"		if ( _short.shortValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 8\");\n" +
				"        }\n" +
				"	}\n" +
				"\n" +
				"	private static void checkLongConversions(Long _long) {\n" +
				"		float f = (float) _long;\n" +
				"		double d = (double) _long;\n" +
				"		if ( _long.longValue() != f ) {\n" +
				"            System.err.println(\"Must be equal 16\");\n" +
				"        }\n" +
				"		if ( _long.longValue() != d ) {\n" +
				"            System.err.println(\"Must be equal 17\");\n" +
				"        }\n" +
				"	}\n" +
				"\n" +
				"    public static void main(String args[]) {\n" +
				"        Byte _byte = new Byte((byte)2);\n" +
				"        Character _character = new Character(\'@\');\n" +
				"        Short _short = new Short((short)255);\n" +
				"        Integer _integer = new Integer(12345678);\n" +
				"        Long _long = new Long(1234567890);\n" +
				"        Float _float = new Float(-0.0);\n" +
				"\n" +
				"        checkByteConversions(_byte);\n" +
				"        checkIntegerConversions(_short);\n" +
				"        checkCharacterConversions(_character);\n" +
				"        checkIntegerConversions(_integer);\n" +
				"        checkLongConversions(_long);\n" +
				"        checkFloatConversions(_float);\n" +
				"\n" +
				"        System.out.println(\"OK\");\n" +
				"      }\n" +
				"}\n"
			},
			"OK"
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84055
	public void test086() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  private static void checkConversions(byte _byte) {\n" +
				"    Short s = (short) _byte; // cast is necessary\n" +
				"    Short s2 = _byte; // ko\n" +
				"  } \n" +
				"  public static void main(String args[]) {\n" +
				"    byte _byte = 2;\n" +
				"    checkConversions(_byte);\n" +
				"    System.out.println(\"OK\");\n" +
				"  }\n" +
				"}\n"
			},
 		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	Short s = (short) _byte; // cast is necessary\n" +
		"	          ^^^^^^^^^^^^^\n" +
		"The expression of type short is boxed into Short\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	Short s2 = _byte; // ko\n" +
		"	      ^^\n" +
		"Type mismatch: cannot convert from byte to Short\n" +
		"----------\n"
        );
	}
    // autoboxing and type argument inference
    public void test087() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "public class X {\n" +
                "    <T> T foo(T t) { return t; }\n" +
                "    \n" +
                "    public static void main(String[] args) {\n" +
                "        int i = new X().foo(12);\n" +
                "        System.out.println(i);\n" +
                "    }\n" +
                "    Zork z;\n" +
                "}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	int i = new X().foo(12);\n" +
			"	        ^^^^^^^^^^^^^^^\n" +
			"The expression of type Integer is unboxed into int\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 5)\n" +
			"	int i = new X().foo(12);\n" +
			"	                    ^^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n"
        );
    }
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=84480 - variation with autoboxing diagnosis on
	 */
	public void test088() {
//		Map customOptions = getCompilerOptions();
//		customOptions.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	int f;\n" +
				"	void foo(int i) {\n" +
				"		i = i++;\n" +
				"		i = ++i;\n" +
				"		f = f++;\n" +
				"		f = ++f;\n" +
				"		Zork z;\n" +
				"	}\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	i = i++;\n" +
			"	^^^^^^^\n" +
			"The assignment to variable i has no effect\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 6)\n" +
			"	f = f++;\n" +
			"	^^^^^^^\n" +
			"The assignment to variable f has no effect\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true,
			null);
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84345
    public void test089() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"  public Object foo() {\n" +
				"  	byte b = 0;\n" +
				"	Number n = (Number) b;\n" +
				"\n" +
				"    java.io.Serializable o = null;\n" +
				"    if (o == 0) return o;\n" +
				"    return this;\n" +
				"  }\n" +
				"}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	Number n = (Number) b;\n" +
			"	           ^^^^^^^^^^\n" +
			"Unnecessary cast from byte to Number\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	Number n = (Number) b;\n" +
			"	                    ^\n" +
			"The expression of type byte is boxed into Byte\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	if (o == 0) return o;\n" +
			"	    ^^^^^^\n" +
			"Incompatible operand types Serializable and int\n" +
			"----------\n"
        );
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84345 - variation
    public void test090() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"  public Object foo() {\n" +
				"  \n" +
				"  	Boolean b = null;\n" +
				"     if (b == true) return b;\n" +
				"     Object o = null;\n" +
				"    if (o == true) return o;\n" +
				"    return this;\n" +
				"  }\n" +
				"}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	if (b == true) return b;\n" +
			"	    ^\n" +
			"The expression of type Boolean is unboxed into boolean\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	if (o == true) return o;\n" +
			"	    ^^^^^^^^^\n" +
			"Incompatible operand types Object and boolean\n" +
			"----------\n"
        );
    }

    // type argument inference and autoboxing
    public void test091() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"\n" +
				"    public static void main(String[] args) {\n" +
				"        Comparable<?> c1 = foo(\"\", new Integer(5));\n" +
				"        Object o = foo(\"\", 5);\n" +
				"    }\n" +
				"    public static <T> T foo(T t1, T t2) { \n" +
				"    	System.out.print(\"foo(\"+t1.getClass().getSimpleName()+\",\"+t2.getClass().getSimpleName()+\")\");\n" +
				"    	return null; \n" +
				"    }\n" +
				"}\n"
            },
			"foo(String,Integer)foo(String,Integer)"
        );
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84669
    public void test092() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X\n" +
				"{\n" +
				"	public X()\n" +
				"	{\n" +
				"		super();\n" +
				"	}\n" +
				"\n" +
				"	public Object convert(Object value)\n" +
				"	{\n" +
				"		Double d = (Double)value;\n" +
				"		d = (d/100);\n" +
				"		return d;\n" +
				"	}\n" +
				"\n" +
				"	public static void main(String[] args)\n" +
				"	{\n" +
				"		X test = new X();\n" +
				"		Object value = test.convert(new Double(50));\n" +
				"		System.out.println(value);\n" +
				"	}\n" +
				"}\n"
            },
			"0.5"
        );
    }

    public void test093() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer someInteger = 12;\n" +
				"		System.out.println((args == null ? someInteger : \'A\') == \'A\');\n" +
				"	}\n" +
				"}\n"
            },
			"true"
        );
    }

    public void test094() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Integer someInteger = 12;\n" +
				"		System.out.println((args == null ? someInteger : \'A\') == \'A\');\n" +
				"		Zork z;\n" +
				"	}\n" +
				"}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Integer someInteger = 12;\n" +
			"	                      ^^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	System.out.println((args == null ? someInteger : \'A\') == \'A\');\n" +
			"	                                   ^^^^^^^^^^^\n" +
			"The expression of type Integer is unboxed into int\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n"
        );
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80630
    public void test095() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = true;\n" +
				"		Character _Character = new Character(\' \');\n" +
				"		char c = \' \';\n" +
				"		Integer _Integer = new Integer(2);\n" +
				"		if ((b ? _Character : _Integer) == c) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		} else {\n" +
				"			System.out.println(\"FAILURE\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
            },
			"SUCCESS"
        );
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80630 - variation
    public void test096() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		boolean b = true;\n" +
				"		Character _Character = new Character(\' \');\n" +
				"		char c = \' \';\n" +
				"		Integer _Integer = new Integer(2);\n" +
				"		if ((b ? _Character : _Integer) == c) {\n" +
				"			System.out.println(zork);\n" +
				"		} else {\n" +
				"			System.out.println(\"FAILURE\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 7)\n" +
			"	if ((b ? _Character : _Integer) == c) {\n" +
			"	         ^^^^^^^^^^\n" +
			"The expression of type Character is unboxed into int\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 7)\n" +
			"	if ((b ? _Character : _Integer) == c) {\n" +
			"	                      ^^^^^^^^\n" +
			"The expression of type Integer is unboxed into int\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	System.out.println(zork);\n" +
			"	                   ^^^^\n" +
			"zork cannot be resolved\n" +
			"----------\n"
        );
    }
    // conditional operator: bool ? Integer : Integer --> Integer (identical operand types)
    // but   bool ? Integer : Short --> unboxed int
    public void test097() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"    public static void main(String args[]) {\n" +
				"        Integer i = 1;\n" +
				"        Integer j = 2;\n" +
				"        Short s = 3;\n" +
				"        foo(args != null ? i : j);\n" +
				"        foo(args != null ? i : s);\n" +
				"    }\n" +
				"    static void foo(int i) {\n" +
				"        System.out.print(\"[int:\"+i+\"]\");\n" +
				"    }\n" +
				"    static void foo(Integer i) {\n" +
				"        System.out.print(\"[Integer:\"+i+\"]\");\n" +
				"    }\n" +
				"}\n"
            },
			"[Integer:1][int:1]"
        );
    }
    // conditional operator: bool ? Integer : Integer --> Integer (identical operand types)
    // but   bool ? Integer : Short --> unboxed int
    // check autoboxing warnings
    public void test098() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"    public static void main(String args[]) {\n" +
				"        Integer i = 1;\n" +
				"        Integer j = 2;\n" +
				"        Short s = 3;\n" +
				"        foo(args != null ? i : j);\n" +
				"        foo(args != null ? i : s);\n" +
				"		 Zork z;\n" +
				"    }\n" +
				"    static void foo(int i) {\n" +
				"        System.out.print(\"[int:\"+i+\"]\");\n" +
				"    }\n" +
				"    static void foo(Integer i) {\n" +
				"        System.out.print(\"[Integer:\"+i+\"]\");\n" +
				"    }\n" +
				"}\n"
            },
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Integer i = 1;\n" +
			"	            ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	Integer j = 2;\n" +
			"	            ^\n" +
			"The expression of type int is boxed into Integer\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 5)\n" +
			"	Short s = 3;\n" +
			"	          ^\n" +
			"The expression of type int is boxed into Short\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 7)\n" +
			"	foo(args != null ? i : s);\n" +
			"	                   ^\n" +
			"The expression of type Integer is unboxed into int\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 7)\n" +
			"	foo(args != null ? i : s);\n" +
			"	                       ^\n" +
			"The expression of type Short is unboxed into int\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 8)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n"
        );
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84801
	public void test099() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends A {\n" +
				"    public void m(Object o) { System.out.println(\"SUCCESS\"); }\n" +
				"    public static void main(String[] args) { ((A) new X()).m(1); }\n" +
				"}\n" +
				"interface I { void m(Object o); }\n" +
				"abstract class A implements I {\n" +
				"	public final void m(int i) {\n" +
				"		System.out.print(\"SUCCESS + \");\n" +
				"		m(new Integer(i));\n" +
				"	}\n" +
				"	public final void m(double d) {\n" +
				"		System.out.print(\"FAILED\");\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS + SUCCESS"
		);
	}
}
