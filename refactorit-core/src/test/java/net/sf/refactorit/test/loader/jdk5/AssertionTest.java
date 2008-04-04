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


public class AssertionTest extends AbstractRegressionTest {

	public AssertionTest(String name) {
		super(name);
	}

	public static Test suite() {
    return new TestSuite(AssertionTest.class);
	}

	public void test001() {
		this.runNegativeTest(
			new String[] {
				"assert.java",
				"public class assert {}\n",
			},
			"----------\n" +
			"1. ERROR in assert.java (at line 1)\n" +
			"	public class assert {}\n" +
			"	             ^^^^^^\n" +
			"Syntax error on token \"assert\", Identifier expected\n" +
			"----------\n");
	}

	public void test002() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "    int i = 4;\n"
			+ "    assert i != 4;\n"
			+ "	   System.out.println(i);\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(\"SUCCESS\");	\n"
			+ "	  } \n"
			+ "	} \n"
			+ "} \n" },
		"SUCCESS", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}

	public void test003() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "    int i = 4;\n"
			+ "    assert i != 4;\n"
			+ "	   System.out.println(i);\n"
			+ "	} \n"
			+ "} \n" },
		"4",
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-da"});
	}
	public void test004() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : \"SUC\";	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		assert false : new Object(){ public String toString(){ return \"CESS\";}};	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"SUCCESS", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test005() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		int i = 2;	\n"
			+ "		assert false : i;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"12", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test006() {
		this.runNegativeTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "	  try {	\n"
			+ "		assert false : unbound;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"----------\n" +
		"1. ERROR in A4.java (at line 4)\n" +
		"	assert false : unbound;	\n" +
		"	               ^^^^^^^\n" +
		"unbound cannot be resolved\n" +
		"----------\n");
	}
	public void test007() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1L;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "   try {	\n"
			+ "		assert false : 0L;	\n" // 0L isn't 0
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		long l = 2L;	\n"
			+ "		assert false : l;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"102", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	public void test008() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1.0f;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		float f = 2.0f;	\n"
			+ "		assert false : f;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"1.02.0", //expected display
		null, // use default class-path
		true, // do not flush previous output dir content
		new String[] {"-ea"});
	}
	public void test009() {
		this.runConformTest(new String[] {
			"A4.java",
			"public class A4 { \n"
			+ "	public static void main(String[] args) {\n"
			+ "   try {	\n"
			+ "		assert false : 1.0;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.print(e.getMessage());	\n"
			+ "	  }	\n"
			+ "	  try {	\n"
			+ "		double d = 2.0;	\n"
			+ "		assert false : d;	\n"
			+ "	  } catch(AssertionError e){	\n"
			+ "		System.out.println(e.getMessage());	\n"
			+ "	  }	\n"
			+ "  }	\n"
			+ "} \n" },
		"1.02.0", //expected display
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}
	// http://dev.eclipse.org/bugs/show_bug.cgi?id=22334
	public void test010() {
		this.runConformTest(new String[] {
			"X.java",
			"public class X { \n" +
			"	public static void main(String[] args) { \n" +
			"		I.Inner inner = new I.Inner(); \n" +
			"		try { \n" +
			"			inner.test(); \n" +
			"			System.out.println(\"FAILED\"); \n" +
			"		} catch(AssertionError e){ \n" +
			"			System.out.println(\"SUCCESS\"); \n" +
			"		} \n" +
			"	} \n" +
			"} \n" +
			"interface I { \n" +
			"  public static class Inner { \n" +
			"    public void test() { \n" +
			"      assert false; \n" +
			"    } \n" +
			"  } \n" +
			"} \n" },
		"SUCCESS",
		null, // use default class-path
		true, // flush previous output dir content
		new String[] {"-ea"});
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28750
	 */
	public void test011() {
		this.runConformTest(
			new String[] {
				"AssertTest.java",
				"public class AssertTest {\n" +
				"   public AssertTest() {}\n" +
				"   public class InnerClass {\n" +
				"      InnerClass() {\n" +
				"        assert(false);\n" +
				"      }\n" +
				"   }\n" +
				"   \n" +
				"   public static void main(String[] args) {	\n" +
				"        System.out.print(\"SUCCESS\");	\n" +
				"	}	\n" +
				"}"
			},
			"SUCCESS"); // expected output
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=57743
	 */
	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main( String[] args ) {\n" +
				"        try {\n" +
				"            throw new Throwable( \"This is a test\");\n" +
				"        }\n" +
				"        catch( Throwable ioe ) {\n" +
				"            assert false : ioe;\n" +
				"        }\n" +
				"        System.out.print(\"SUCCESS\");	\n" +
				"    }\n" +
				"}\n"
			},
			"SUCCESS"); // expected output
	}

}
