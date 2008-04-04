/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader.jdk5;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AnnotationTest extends AbstractComparisonTest {

  String reportMissingJavadocComments = null;

  public AnnotationTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(AnnotationTest.class);
  }

  public static Class testClass() {
    return AnnotationTest.class;
  }

//	protected Map getCompilerOptions() {
//		Map options = super.getCompilerOptions();
//		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
//		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
//		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
//		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
//		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
//		if (reportMissingJavadocComments != null)
//			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportMissingJavadocComments);
//		return options;
//	}
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    reportMissingJavadocComments = null;
  }

  public void test001() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public @interface X { \n" +
        "	String value(); \n" +
        "}"
    },
        "");
  }

  // check invalid annotation
  public void test002() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public @Foo class X {\n" +
        "}\n" +
        "\n" +
        "@interface Foo {\n" +
        "	String value();\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	public @Foo class X {\n" +
        "	       ^^^^\n" +
        "The annotation @Foo must define the attribute value\n" +
        "----------\n");
  }

  // check annotation method cannot indirectly return annotation type (circular ref)
  // TODO (kent) reenable once addressed
  public void _test003() {
    this.runNegativeTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "	Bar value();\n" +
        "}\n" +
        "\n" +
        "@interface Bar {\n" +
        "	Foo value();\n" +
        "}\n"
    },
        "invalid circular reference to annotation");
  }

  // check annotation method cannot directly return annotation type
  public void test004() {
    this.runNegativeTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "	Foo value();\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in Foo.java (at line 2)\n" +
        "	Foo value();\n" +
        "	^^^\n" +
        "Cycle detected: the annotation type Foo cannot contain attributes of the annotation type itself\n" +
        "----------\n");
  }

  // check annotation type cannot have superclass
  public void test005() {
    this.runNegativeTest(
        new String[] {
        "Foo.java",
        "public @interface Foo extends Object {\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in Foo.java (at line 1)\n" +
        "	public @interface Foo extends Object {\n" +
        "	                  ^^^\n" +
        "Annotation type declaration cannot have an explicit superclass\n" +
        "----------\n");
  }

  // check annotation type cannot have superinterfaces
  public void test006() {
    this.runNegativeTest(
        new String[] {
        "Foo.java",
        "public @interface Foo implements Cloneable {\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in Foo.java (at line 1)\n" +
        "	public @interface Foo implements Cloneable {\n" +
        "	                  ^^^\n" +
        "Annotation type declaration cannot have explicit superinterfaces\n" +
        "----------\n");
  }

  // check annotation method cannot be specified parameters
  // TODO (olivier) unoptimal syntax error -> no parameter for annotation method?
  public void test007() {
    this.runNegativeTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "	String value(int i);\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in Foo.java (at line 2)\n" +
        "	String value(int i);\n" +
        "	       ^^^^^^^^^^^^\n" +
        "Annotation attributes cannot have parameters\n" +
        "----------\n");
  }

  // annotation method cannot be generic?
  public void test008() {
    this.runNegativeTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "	<T> T value();\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in Foo.java (at line 2)\n" +
        "	<T> T value();\n" +
        "	    ^\n" +
        "Invalid type T for the annotation attribute Foo.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
        "----------\n" +
        "2. ERROR in Foo.java (at line 2)\n" +
        "	<T> T value();\n" +
        "	      ^^^^^^^\n" +
        "Annotation attributes cannot be generic\n" +
        "----------\n");
  }

  // check annotation method return type
  public void test009() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public @interface X {\n" +
        "	\n" +
        "	Runnable value();\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 3)\n" +
        "	Runnable value();\n" +
        "	^^^^^^^^\n" +
        "Invalid type Runnable for the annotation attribute X.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
        "----------\n");
  }

  // check annotation method missing return type
  // TODO (olivier) we should get rid of syntax error here (tolerate invalid constructor scenario)
  public void test010() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public @interface X {\n" +
        "	\n" +
        "	value();\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 3)\n" +
        "	value();\n" +
        "	^^^^^^^\n" +
        "Return type for the method is missing\n" +
        "----------\n");
  }

  // check annotation denotes annotation type
  public void test011() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@Object\n" +
        "public class X {\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	@Object\n" +
        "	 ^^^^^^\n" +
        "Type mismatch: cannot convert from Object to Annotation\n" +
        "----------\n");
  }

  // check for duplicate annotations
  public void test012() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@Foo @Foo\n" +
        "public class X {\n" +
        "}\n" +
        "@interface Foo {}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	@Foo @Foo\n" +
        "	^^^^\n" +
        "Duplicate annotation @Foo\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 1)\n" +
        "	@Foo @Foo\n" +
        "	     ^^^^\n" +
        "Duplicate annotation @Foo\n" +
        "----------\n");
  }

  // check single member annotation - no need to specify value if member has default value
  public void test013() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@Foo(\"hello\") public class X {\n" +
        "}\n" +
        "\n" +
        "@interface Foo {\n" +
        "	String id() default \"\";\n" +
        "	String value() default \"\";\n" +
        "}\n"
    },
        "");
  }

  // check single member annotation -  need to speficy value if member has no default value
  public void test014() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@Foo(\"hello\") public class X {\n" +
        "}\n" +
        "\n" +
        "@interface Foo {\n" +
        "	String id() default \"\";\n" +
        "	String value() default \"\";\n" +
        "	String foo();\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	@Foo(\"hello\") public class X {\n" +
        "	^^^^\n" +
        "The annotation @Foo must define the attribute foo\n" +
        "----------\n");
  }

  // check normal annotation -  need to speficy value if member has no default value
  public void test015() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@Foo(\n" +
        "		id = \"hello\") public class X {\n" +
        "}\n" +
        "\n" +
        "@interface Foo {\n" +
        "	String id() default \"\";\n" +
        "	String value() default \"\";\n" +
        "	String foo();\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	@Foo(\n" +
        "	^^^^\n" +
        "The annotation @Foo must define the attribute foo\n" +
        "----------\n");
  }

  // check normal annotation - if single member, no need to be named 'value'
  public void test016() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface Name {\n" +
        "	String first();\n" +
        "	String last();\n" +
        "}\n" +
        "@interface Author {\n" +
        "	Name name();\n" +
        "}\n" +
        "public class X {\n" +
        "	\n" +
        "	@Author(name = @Name(first=\"Bill\", last=\"Yboy\")) \n" +
        "	void foo() {\n" +
        "	}\n" +
        "}\n"
    },
        "");
  }

  // check single member annotation can only refer to 'value' member
  public void test017() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface Name {\n" +
        "	String first();\n" +
        "	String last();\n" +
        "}\n" +
        "@interface Author {\n" +
        "	Name name();\n" +
        "}\n" +
        "@Author(@Name(first=\"Joe\",last=\"Hacker\")) \n" +
        "public class X {\n" +
        "	\n" +
        "	@Author(name = @Name(first=\"Bill\", last=\"Yboy\")) \n" +
        "	void foo() {\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	@Author(@Name(first=\"Joe\",last=\"Hacker\")) \n" +
        "	^^^^^^^\n" +
        "The annotation @Author must define the attribute name\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 8)\n" +
        "	@Author(@Name(first=\"Joe\",last=\"Hacker\")) \n" +
        "	        ^^^^^\n" +
        "The attribute value is undefined for the annotation type Author\n" +
        "----------\n");
  }

  // check for duplicate member value pairs
  public void test018() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface Name {\n" +
        "	String first();\n" +
        "	String last();\n" +
        "}\n" +
        "@interface Author {\n" +
        "	Name name();\n" +
        "}\n" +
        "public class X {\n" +
        "	\n" +
        "	@Author(name = @Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")) \n" +
        "	void foo() {\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 10)\n" +
        "	@Author(name = @Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")) \n" +
        "	                                   ^^^^\n" +
        "Duplicate attribute last in annotation @Name\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 10)\n" +
        "	@Author(name = @Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")) \n" +
        "	                                                ^^^^\n" +
        "Duplicate attribute last in annotation @Name\n" +
        "----------\n");
  }

  // check class annotation member value must be a class literal
  public void test019() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface Foo {\n" +
        "	Class value() default X.clazz();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "	@Foo( clazz() )\n" +
        "	void foo() {}\n" +
        "	static Class clazz() { return X.class; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	Class value() default X.clazz();\n" +
        "	                      ^^^^^^^^^\n" +
        "The value for annotation attribute Foo.value must be a class literal\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 6)\n" +
        "	@Foo( clazz() )\n" +
        "	      ^^^^^^^\n" +
        "The value for annotation attribute Foo.value must be a class literal\n" +
        "----------\n");
  }

  // check primitive annotation member value must be a constant
  public void test020() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface Foo {\n" +
        "	int value() default X.val();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "	@Foo( val() )\n" +
        "	void foo() {}\n" +
        "	static int val() { return 0; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	int value() default X.val();\n" +
        "	                    ^^^^^^^\n" +
        "The value for annotation attribute Foo.value must be a constant expression\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 6)\n" +
        "	@Foo( val() )\n" +
        "	      ^^^^^\n" +
        "The value for annotation attribute Foo.value must be a constant expression\n" +
        "----------\n");
  }

  // check String annotation member value must be a constant
  public void test021() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface Foo {\n" +
        "	String value() default X.val();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "	@Foo( val() )\n" +
        "	void foo() {}\n" +
        "	static String val() { return \"\"; }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	String value() default X.val();\n" +
        "	                       ^^^^^^^\n" +
        "The value for annotation attribute Foo.value must be a constant expression\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 6)\n" +
        "	@Foo( val() )\n" +
        "	      ^^^^^\n" +
        "The value for annotation attribute Foo.value must be a constant expression\n" +
        "----------\n");
  }

  // check String annotation member value must be a constant
  public void test022() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface Foo {\n" +
        "	String[] value() default null;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "	@Foo( null )\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	String[] value() default null;\n" +
        "	                         ^^^^\n" +
        "The value for annotation attribute Foo.value must be a constant expression\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 6)\n" +
        "	@Foo( null )\n" +
        "	      ^^^^\n" +
        "The value for annotation attribute Foo.value must be a constant expression\n" +
        "----------\n");
  }

  // check use of array initializer
  public void test023() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface Foo {\n" +
        "	String[] value() default {};\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "	@Foo( {} )\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "");
  }

  // check use of binary annotation - check referencing binary annotation
  public void test024() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "	String[] value() default {};\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo({})\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test025() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "	String[] value() default {};\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test026() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "		int value() default 8;\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test027() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "		byte value() default (byte)255;\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test028() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "		boolean value() default true;\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test029() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "		char value() default ' ';\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test030() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "		short value() default (short)1024;\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test031() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "		double value() default 0.0;\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test032() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "		float value() default -0.0f;\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test033() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "		long value() default 1234567890L;\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test034() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "public @interface Foo {\n" +
        "		String value() default \"Hello, World\";\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test035() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "enum E {\n" +
        "	CONST1\n" +
        "}\n" +
        "@interface Foo {\n" +
        "	E value() default E.CONST1;\n" +
        "}"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test036() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "@interface Foo {\n" +
        "	Class value() default Object.class;\n" +
        "}"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test037() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "@interface Y {\n" +
        "	int id() default 8;\n" +
        "	Class type();\n" +
        "}\n" +
        "public @interface Foo {\n" +
        "	Y value() default @Y(id=10,type=Object.class);\n" +
        "}"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "	@Foo()\n" +
        "	void foo() {}\n" +
        "}\n"
    },
        "",
        null,
        false,
        null);
  }

  // check use of binary annotation - check default value presence
  public void test038() {
    this.runConformTest(
        new String[] {
        "Foo.java",
        "@interface Foo {\n" +
        "	int id() default 8;\n" +
        "	Class type();\n" +
        "}"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "@Foo(type=String.class) public class X {\r\n" +
        "}"
    },
        "",
        null,
        false,
        null);
  }

  // check annotation member modifiers
  public void test039() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public @interface X {\n" +
        "	native int id() default 0;\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	native int id() default 0;\n" +
        "	           ^^^^\n" +
        "Illegal modifier for the annotation attribute X.id; only public & abstract are permitted\n" +
        "----------\n");
  }

  // check annotation array field initializer
  public void test040() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public @interface X {\n" +
        "	int[] tab;\n" +
        "	int[] value();\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	int[] tab;\n" +
        "	      ^^^\n" +
        "The annotation field X.tab must be initialized with a constant expression\n" +
        "----------\n");
  }

  // check annotation array field initializer
  public void test041() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public @interface X {\n" +
        "	int[] tab = value();\n" +
        "	int[] value();\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	int[] tab = value();\n" +
        "	            ^^^^^\n" +
        "Cannot make a static reference to the non-static method value() from the type X\n" +
        "----------\n");
  }

  // check annotation array field initializer
  public void test042() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public @interface X {\n" +
        "	int[] tab = { 0 , \"aaa\".length() };\n" +
        "}\n"
    },
        "");
  }

  // check annotation field initializer
  public void test043() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public @interface X {\n" +
        "	int value;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\r\n" +
        "	int value;\r\n" +
        "	    ^^^^^\n" +
        "The annotation field X.value must be initialized with a constant expression\n" +
        "----------\n");
  }

  // check annotation field initializer
  public void test044() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public @interface X {\n" +
        "	protected int value = 0;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	protected int value = 0;\n" +
        "	              ^^^^^\n" +
        "Illegal modifier for the annotation field X.value; only public, static & final are permitted\n" +
        "----------\n");
  }

  // check incompatible default values
  public void test045() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface X {\n" +
        "    int id () default 10L; \n" +
        "    int[] ids() default { 10L };\n" +
        "    Class cls() default new Object();\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	int id () default 10L; \n" +
        "	                  ^^^\n" +
        "Type mismatch: cannot convert from long to int\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 3)\n" +
        "	int[] ids() default { 10L };\n" +
        "	                      ^^^\n" +
        "Type mismatch: cannot convert from long to int\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 4)\n" +
        "	Class cls() default new Object();\n" +
        "	                    ^^^^^^^^^^^^\n" +
        "Type mismatch: cannot convert from Object to Class\n" +
        "----------\n");
  }

  // check need for constant pair value
  public void test046() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    boolean val() default true;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "\n" +
        "	boolean bar() {\n" +
        "		return false;\n" +
        "	}\n" +
        "    @I(val = bar()) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 10)\n" +
        "	@I(val = bar()) void foo() {\n" +
        "	         ^^^^^\n" +
        "The value for annotation attribute I.val must be a constant expression\n" +
        "----------\n");
  }

  // check array handling of singleton
  public void test047() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    boolean[] val() default {true};\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(val = false) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");

//		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
//		String actualOutput = null;
//		try {
//			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
//			actualOutput =
//				disassembler.disassemble(
//					classFileBytes,
//					"\n",
//					ClassFileBytesDisassembler.DETAILED);
//		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
//			assertTrue("ClassFormatException", false);
//		} catch (IOException e) {
//			assertTrue("IOException", false);
//		}
//
//		String expectedOutput =
//			"  // Method descriptor  #6 ()V\n" +
//			"  // Stack: 0, Locals: 1\n" +
//			"  @I(val={false})\n" +
//			"  void foo();\n" +
//			"    0  return\n" +
//			"      Line numbers:\n" +
//			"        [pc: 0, line: 7]\n" +
//			"      Local variable table:\n" +
//			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" +
//			"}";
//
//		if (actualOutput.indexOf(expectedOutput) == -1) {
//			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
//		}
//		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
//
//		try {
//			ClassFileReader fileReader = ClassFileReader.read(new File(OUTPUT_DIR + File.separator  +"I.class"));
//			assertEquals("Not an annotation type declaration", IGenericType.ANNOTATION_TYPE_DECL, fileReader.getKind());
//		} catch (ClassFormatException e1) {
//			assertTrue("ClassFormatException", false);
//		} catch (IOException e1) {
//			assertTrue("IOException", false);
//		}
  }

  // check invalid constant in array initializer
  public void test048() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "     boolean[] value();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "     @I(value={false, X.class != null }) void foo() {\n" +
        "     }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	@I(value={false, X.class != null }) void foo() {\n" +
        "	                 ^^^^^^^^^^^^^^^\n" +
        "The value for annotation attribute I.value must be a constant expression\n" +
        "----------\n");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=79349
  public void test049() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.*;\n" +
        "\n" +
        "@Documented\n" +
        "@Retention(RetentionPolicy.RUNTIME)\n" +
        "@Target(ElementType.TYPE)\n" +
        "@interface MyAnn {\n" +
        "  String value() default \"Default Message\";\n" +
        "}\n" +
        "\n" +
        "@MyAnn\n" +
        "public class X {\n" +
        "	public @MyAnn void something() { }	\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 12)\r\n" +
        "	public @MyAnn void something() { }	\r\n" +
        "	       ^^^^^^\n" +
        "The annotation @MyAnn is disallowed for this location\n" +
        "----------\n");
  }

  // check array handling of singleton
  public void test050() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    String[] value();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(\"Hello\") void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");

//		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
//		String actualOutput = null;
//		try {
//			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
//			actualOutput =
//				disassembler.disassemble(
//					classFileBytes,
//					"\n",
//					ClassFileBytesDisassembler.DETAILED);
//		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
//			assertTrue("ClassFormatException", false);
//		} catch (IOException e) {
//			assertTrue("IOException", false);
//		}
//
//		String expectedOutput =
//			"  // Method descriptor  #6 ()V\n" +
//			"  // Stack: 0, Locals: 1\n" +
//			"  @I(value={\"Hello\"})\n" +
//			"  void foo();\n" +
//			"    0  return\n" +
//			"      Line numbers:\n" +
//			"        [pc: 0, line: 7]\n" +
//			"      Local variable table:\n" +
//			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" +
//			"}";
//
//		if (actualOutput.indexOf(expectedOutput) == -1) {
//			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
//		}
//		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
  }

  public void test051() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    String value() default \"Hello\";\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(\"Hi\") void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");

//		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
//		String actualOutput = null;
//		try {
//			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
//			actualOutput =
//				disassembler.disassemble(
//					classFileBytes,
//					"\n",
//					ClassFileBytesDisassembler.DETAILED);
//		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
//			assertTrue("ClassFormatException", false);
//		} catch (IOException e) {
//			assertTrue("IOException", false);
//		}
//
//		String expectedOutput =
//			"  // Method descriptor  #6 ()V\n" +
//			"  // Stack: 0, Locals: 1\n" +
//			"  @I(value=\"Hi\")\n" +
//			"  void foo();\n" +
//			"    0  return\n" +
//			"      Line numbers:\n" +
//			"        [pc: 0, line: 7]\n" +
//			"      Local variable table:\n" +
//			"        [pc: 0, pc: 1] local: this index: 0 type: LX;\n" +
//			"}";
//
//		if (actualOutput.indexOf(expectedOutput) == -1) {
//			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
//		}
//		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
  }

  public void test052() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    int value() default 0;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(2) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");

  }

  public void test053() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    byte value() default 0;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(2) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");

  }

  public void test054() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    short value() default 0;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(2) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  public void test055() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    char value() default ' ';\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I('@') void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  public void test056() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    long value() default 6;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(Long.MAX_VALUE) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  public void test057() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    float value();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(-0.0f) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  public void test058() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    double value();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(-0.0) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  public void test059() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface Foo {\n" +
        "    double value() default 0.0;\n" +
        "    int id();\n" +
        "}\n" +
        "@interface I {\n" +
        "    Foo value();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(@Foo(id=5)) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  public void test060() {
    this.runConformTest(
        new String[] {
        "X.java",
        "enum Color {" +
        "	BLUE, RED, GREEN\n" +
        "}\n" +
        "@interface I {\n" +
        "    Color value() default Color.GREEN;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(Color.RED) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  public void test061() {
    this.runConformTest(
        new String[] {
        "X.java",
        "enum Color {" +
        "	BLUE, RED, GREEN\n" +
        "}\n" +
        "@interface I {\n" +
        "    Color[] value() default { Color.GREEN };\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(Color.RED) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  public void test062() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface Foo {\n" +
        "    double value() default 0.0;\n" +
        "    int id() default 0;\n" +
        "}\n" +
        "enum Color {" +
        "	BLUE, RED, GREEN\n" +
        "}\n" +
        "@interface I {\n" +
        "    Color[] enums() default { Color.GREEN };\n" +
        "    Foo[] annotations() default { @Foo() };\n" +
        "    int[] ints() default { 0, 1, 2, 3 };\n" +
        "    byte[] bytes() default { 0 };\n" +
        "    short[] shorts() default { 0 };\n" +
        "    long[] longs() default { Long.MIN_VALUE, Long.MAX_VALUE };\n" +
        "    String[] strings() default { \"\" };\n" +
        "    boolean[] booleans() default { true, false };\n" +
        "    float[] floats() default { Float.MAX_VALUE };\n" +
        "    double[] doubles() default { Double.MAX_VALUE };\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(enums=Color.RED,\n" +
        "		annotations=@Foo(),\n" +
        "		ints=2,\n" +
        "		bytes=1,\n" +
        "		shorts=5,\n" +
        "		longs=Long.MIN_VALUE,\n" +
        "		strings=\"Hi\",\n" +
        "		booleans=true,\n" +
        "		floats=0.0f,\n" +
        "		doubles=-0.0) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  public void test063() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface Foo {\n" +
        "    double value() default 0.0;\n" +
        "    int id() default 0;\n" +
        "}\n" +
        "enum Color {" +
        "	BLUE, RED, GREEN\n" +
        "}\n" +
        "@interface I {\n" +
        "    Color enums() default Color.GREEN;\n" +
        "    Foo annotations() default @Foo();\n" +
        "    int ints() default 0;\n" +
        "    byte bytes() default 0;\n" +
        "    short shorts() default 0;\n" +
        "    long longs() default Long.MIN_VALUE;\n" +
        "    String strings() default \"\";\n" +
        "    boolean booleans() default true;\n" +
        "    float floats() default Float.MAX_VALUE;\n" +
        "    double doubles() default Double.MAX_VALUE;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(enums=Color.RED,\n" +
        "		annotations=@Foo(),\n" +
        "		ints=2,\n" +
        "		bytes=1,\n" +
        "		shorts=5,\n" +
        "		longs=Long.MIN_VALUE,\n" +
        "		strings=\"Hi\",\n" +
        "		booleans=true,\n" +
        "		floats=0.0f,\n" +
        "		doubles=-0.0) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  public void test064() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    String[] names();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(names={\"Hello\"}) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=79848
  public void test065() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    Class[] classes();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(classes = {X.class, I.class}) public void foo(){\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844
  public void test066() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    short value() default 0;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(2) void foo() {\n" +
        "    }\n" +
        "}\n" +
        "\n"
    },
        "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844 - variation
  public void test067() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    int value() default 0L;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(2) void foo() {\n" +
        "    }\n" +
        "}\n" +
        "\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	int value() default 0L;\n" +
        "	                    ^^\n" +
        "Type mismatch: cannot convert from long to int\n" +
        "----------\n");
  }

  // 79844 - variation
  public void test068() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    short[] value() default 2;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(2) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844 - variation
  public void test069() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    short[] value() default { 2 };\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(2) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=79847
  public void test070() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "    int[][] ids();\n" +
        "    Object[][] obs();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "\n" +
        "    @I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	int[][] ids();\n" +
        "	^^^^^^^\n" +
        "Invalid type int[][] for the annotation attribute I.ids; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 3)\n" +
        "	Object[][] obs();\n" +
        "	^^^^^^^^^^\n" +
        "Invalid type Object[][] for the annotation attribute I.obs; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 8)\n" +
        "	@I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" +
        "	^^\n" +
        "The annotation @I must define the attribute obs\n" +
        "----------\n" +
        "4. ERROR in X.java (at line 8)\n" +
        "	@I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" +
        "	          ^^^^^^^\n" +
        "The value for annotation attribute I.ids must be a constant expression\n" +
        "----------\n" +
        "5. ERROR in X.java (at line 8)\n" +
        "	@I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" +
        "	                   ^^^^^\n" +
        "The value for annotation attribute I.ids must be a constant expression\n" +
        "----------\n");
  }

  // check annotation type cannot override any supertype method
  public void test071() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "	int hashCode();\n" +
        "	Object clone();\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(hashCode = 0) public void foo(){\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	int hashCode();\n" +
        "	    ^^^^^^^^^^\n" +
        "The annotation type I cannot override the method Annotation.hashCode()\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 3)\n" +
        "	Object clone();\n" +
        "	^^^^^^\n" +
        "Invalid type Object for the annotation attribute I.clone; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
        "----------\n" +
        "3. ERROR in X.java (at line 3)\n" +
        "	Object clone();\n" +
        "	       ^^^^^^^\n" +
        "The annotation type I cannot override the method Object.clone()\n" +
        "----------\n" +
        "4. ERROR in X.java (at line 7)\n" +
        "	@I(hashCode = 0) public void foo(){\n" +
        "	^^\n" +
        "The annotation @I must define the attribute clone\n" +
        "----------\n");
  }

  // check annotation cannot refer to inherited methods as attributes
  public void test072() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(hashCode = 0) public void foo(){\n" +
        "    }\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 5)\n" +
        "	@I(hashCode = 0) public void foo(){\n" +
        "	   ^^^^^^^^\n" +
        "The attribute hashCode is undefined for the annotation type I\n" +
        "----------\n");
  }

  // check code generation of annotation default attribute (autowrapping)
  public void test073() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface Foo {\n" +
        "    double value() default 0.0;\n" +
        "    int id() default 0;\n" +
        "}\n" +
        "enum Color {" +
        "	BLUE, RED, GREEN\n" +
        "}\n" +
        "@interface I {\n" +
        "    Color[] enums() default Color.GREEN;\n" +
        "    Foo[] annotations() default @Foo();\n" +
        "    int[] ints() default 0;\n" +
        "    byte[] bytes() default 1;\n" +
        "    short[] shorts() default 3;\n" +
        "    long[] longs() default Long.MIN_VALUE;\n" +
        "    String[] strings() default \"\";\n" +
        "    boolean[] booleans() default true;\n" +
        "    float[] floats() default Float.MAX_VALUE;\n" +
        "    double[] doubles() default Double.MAX_VALUE;\n" +
        "    Class[] classes() default I.class;\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "    @I(enums=Color.RED,\n" +
        "		annotations=@Foo(),\n" +
        "		ints=2,\n" +
        "		bytes=1,\n" +
        "		shorts=5,\n" +
        "		longs=Long.MIN_VALUE,\n" +
        "		strings=\"Hi\",\n" +
        "		booleans=true,\n" +
        "		floats=0.0f,\n" +
        "		doubles=-0.0) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  // check code generation of annotation default attribute non array types
  public void test074() {
    this.runConformTest(
        new String[] {
        "X.java",
        "@interface Foo {\n" +
        "    double value() default 0.0;\n" +
        "    int id() default 0;\n" +
        "}\n" +
        "enum Color {" +
        "	BLUE, RED, GREEN\n" +
        "}\n" +
        "@interface I {\n" +
        "    Color _enum() default Color.GREEN;\n" +
        "    Foo _annotation() default @Foo();\n" +
        "    int _int() default 0;\n" +
        "    byte _byte() default 1;\n" +
        "    short _short() default 3;\n" +
        "    long _long() default Long.MIN_VALUE;\n" +
        "    String _string() default \"\";\n" +
        "    boolean _boolean() default true;\n" +
        "    float _float() default Float.MAX_VALUE;\n" +
        "    double _double() default Double.MAX_VALUE;\n" +
        "    Class _class() default I.class;\n" +
        "}\n" +
        "public class X {\n" +
        "    @I(_enum=Color.RED,\n" +
        "		_annotation=@Foo(),\n" +
        "		_int=2,\n" +
        "		_byte=1,\n" +
        "		_short=5,\n" +
        "		_long=Long.MIN_VALUE,\n" +
        "		_string=\"Hi\",\n" +
        "		_boolean=true,\n" +
        "		_float=0.0f,\n" +
        "		_double=-0.0) void foo() {\n" +
        "    }\n" +
        "}\n"
    },
        "");
  }

  // check detection of duplicate target element specification
  public void test075() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Target;\n" +
        "import static java.lang.annotation.ElementType.*;\n" +
        "\n" +
        "@Target ({FIELD, FIELD})\n" +
        "@interface Tgt {\n" +
        "	E[] foo();\n" +
        "	int[] bar();\n" +
        "}\n" +
        "enum E {\n" +
        "	BLEU, BLANC, ROUGE\n" +
        "}\n" +
        "\n" +
        "@Tgt( foo = { E.BLEU, E.BLEU}, bar = { 0, 0} )\n" +
        "public class X {\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	@Target ({FIELD, FIELD})\n" +
        "	                 ^^^^^\n" +
        "Duplicate element FIELD specified in annotation @Target\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 13)\n" +
        "	@Tgt( foo = { E.BLEU, E.BLEU}, bar = { 0, 0} )\n" +
        "	^^^^\n" +
        "The annotation @Tgt is disallowed for this location\n" +
        "----------\n");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=77463
  public void test076() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "private @interface TestAnnot {\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	private @interface TestAnnot {\n" +
        "	                   ^^^^^^^^^\n" +
        "Illegal modifier for the annotation type TestAnnot; only public & abstract are permitted\n" +
        "----------\n");
  }

  // check @Override annotation - strictly for superclasses (overrides) and not interfaces (implements)
  public void test077() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "class Further {\n" +
        "	void bar() {}\n" +
        "}\n" +
        "\n" +
        "class Other extends Further {\n" +
        "}\n" +
        "\n" +
        "interface Baz {\n" +
        "	void baz();\n" +
        "}\n" +
        "\n" +
        "public class X extends Other implements Baz {\n" +
        "	@Override\n" +
        "	void foo() {}\n" +
        "	@Override\n" +
        "	void bar() {}\n" +
        "	@Override\n" +
        "	public void baz() {}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 14)\n" +
        "	void foo() {}\n" +
        "	     ^^^^^\n" +
        "The method foo() of type X must override a superclass method\n" +
        "----------\n" +
        "2. ERROR in X.java (at line 18)\n" +
        "	public void baz() {}\n" +
        "	            ^^^^^\n" +
        "The method baz() of type X must override a superclass method\n" +
        "----------\n");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80114
  public void test078() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "public @interface X {\n" +
        "	X() {}\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	X() {}\n" +
        "	^^^\n" +
        "Annotation type declaration cannot have a constructor\n" +
        "----------\n");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
  public void test079() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Retention;\n" +
        "import java.lang.annotation.Target;\n" +
        "import static java.lang.annotation.RetentionPolicy.*;\n" +
        "import static java.lang.annotation.ElementType.*;\n" +
        "\n" +
        "@Retention(RUNTIME)\n" +
        "@Target({TYPE})\n" +
        "@interface Attr {\n" +
        "  public int tst() default -1;\n" +
        "}\n" +
        "\n" +
        "@Attr \n" +
        "public class X {\n" +
        "  public static void main(String args[]) {\n" +
        "  	Object e = X.class.getAnnotation(Attr.class);\n" +
        "  	System.out.print(e);\n" +
        "  }\n" +
        "}"
    },
        "@Attr(tst=-1)");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
  public void test080() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Retention;\n" +
        "import java.lang.annotation.Target;\n" +
        "import static java.lang.annotation.RetentionPolicy.*;\n" +
        "import static java.lang.annotation.ElementType.*;\n" +
        "\n" +
        "@Retention(CLASS)\n" +
        "@Target({TYPE})\n" +
        "@interface Attr {\n" +
        "  public int tst() default -1;\n" +
        "}\n" +
        "\n" +
        "@Attr \n" +
        "public class X {\n" +
        "  public static void main(String args[]) {\n" +
        "  	Object e = X.class.getAnnotation(Attr.class);\n" +
        "  	System.out.print(e);\n" +
        "  }\n" +
        "}"
    },
        "null");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
  public void test081() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Retention;\n" +
        "import java.lang.annotation.Target;\n" +
        "import static java.lang.annotation.RetentionPolicy.*;\n" +
        "import static java.lang.annotation.ElementType.*;\n" +
        "\n" +
        "@Retention(SOURCE)\n" +
        "@Target({TYPE})\n" +
        "@interface Attr {\n" +
        "  public int tst() default -1;\n" +
        "}\n" +
        "\n" +
        "@Attr \n" +
        "public class X {\n" +
        "  public static void main(String args[]) {\n" +
        "  	Object e = X.class.getAnnotation(Attr.class);\n" +
        "  	System.out.print(e);\n" +
        "  }\n" +
        "}"
    },
        "null");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
  public void test082() {
    this.runConformTest(
        new String[] {
        "Attr.java",
        "import java.lang.annotation.Retention;\n" +
        "import java.lang.annotation.Target;\n" +
        "import static java.lang.annotation.RetentionPolicy.*;\n" +
        "import static java.lang.annotation.ElementType.*;\n" +
        "\n" +
        "@Retention(SOURCE)\n" +
        "@Target({TYPE})\n" +
        "@interface Attr {\n" +
        "  public int tst() default -1;\n" +
        "}",
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "@Attr \n" +
        "public class X {\n" +
        "  public static void main(String args[]) {\n" +
        "  	Object e = X.class.getAnnotation(Attr.class);\n" +
        "  	System.out.print(e);\n" +
        "  }\n" +
        "}"
    },
        "null",
        null,
        false,
        null);
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
  public void test083() {
    this.runConformTest(
        new String[] {
        "Attr.java",
        "import java.lang.annotation.Retention;\n" +
        "import java.lang.annotation.Target;\n" +
        "import static java.lang.annotation.RetentionPolicy.*;\n" +
        "import static java.lang.annotation.ElementType.*;\n" +
        "\n" +
        "@Retention(CLASS)\n" +
        "@Target({TYPE})\n" +
        "@interface Attr {\n" +
        "  public int tst() default -1;\n" +
        "}",
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "@Attr \n" +
        "public class X {\n" +
        "  public static void main(String args[]) {\n" +
        "  	Object e = X.class.getAnnotation(Attr.class);\n" +
        "  	System.out.print(e);\n" +
        "  }\n" +
        "}"
    },
        "null",
        null,
        false,
        null);
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
  public void test084() {
    this.runConformTest(
        new String[] {
        "Attr.java",
        "import java.lang.annotation.Retention;\n" +
        "import java.lang.annotation.Target;\n" +
        "import static java.lang.annotation.RetentionPolicy.*;\n" +
        "import static java.lang.annotation.ElementType.*;\n" +
        "\n" +
        "@Retention(RUNTIME)\n" +
        "@Target({TYPE})\n" +
        "@interface Attr {\n" +
        "  public int tst() default -1;\n" +
        "}",
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "@Attr \n" +
        "public class X {\n" +
        "  public static void main(String args[]) {\n" +
        "  	Object e = X.class.getAnnotation(Attr.class);\n" +
        "  	System.out.print(e);\n" +
        "  }\n" +
        "}"
    },
        "@Attr(tst=-1)",
        null,
        false,
        null);
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=76751
  public void test085() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.ElementType;\n" +
        "import java.lang.annotation.RetentionPolicy;\n" +
        "import java.lang.annotation.Target;\n" +
        "import java.lang.annotation.Retention;\n" +
        "\n" +
        "public class X {\n" +
        "\n" +
        "  @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface\n" +
        "TestAnnotation {\n" +
        "\n" +
        "    String testAttribute();\n" +
        "\n" +
        "  }\n" +
        "  @TestAnnotation(testAttribute = \"test\") class A {\n" +
        "  }\n" +
        "\n" +
        "  public static void main(String[] args) {\n" +
        "    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));\n" +
        "  }\n" +
        "\n" +
        "}"
    },
        "true");
  }

  // check handling of empty array initializer
  public void test086() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Target;\n" +
        "\n" +
        "@Target({}) @interface I {}\n" +
        "@I public class X {}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\n" +
        "	@I public class X {}\n" +
        "	^^\n" +
        "The annotation @I is disallowed for this location\n" +
        "----------\n");
  }

  // check type targeting annotation also allowed for annotation type
  public void test087() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Target;\n" +
        "import static java.lang.annotation.ElementType.*;\n" +
        "\n" +
        "@Target(TYPE)\n" +
        "@interface Annot {\n" +
        "}\n" +
        "\n" +
        "@Annot\n" +
        "public @interface X {\n" +
        "}\n"
    },
        "");
  }

  // check parameter/local target for annotation
  public void test088() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Target;\n" +
        "import static java.lang.annotation.ElementType.*;\n" +
        "\n" +
        "@Target(LOCAL_VARIABLE)\n" +
        "@interface Annot {\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "	void foo(@Annot int i) {\n" +
        "		@Annot int j;\n" +
        "	}\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 9)\n" +
        "	void foo(@Annot int i) {\n" +
        "	         ^^^^^^\n" +
        "The annotation @Annot is disallowed for this location\n" +
        "----------\n");
  }

  // Add check for parameter
  public void test089() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Target;\n" +
        "import java.lang.annotation.ElementType;\n" +
        "\n" +
        "public class X {\n" +
        "\n" +
        "    @Target(ElementType.PARAMETER) @interface I {}\n" +
        "    \n" +
        "    void m(@I int i){\n" +
        "    }\n" +
        "}"
    },
        "");
  }

  // Add check that type includes annotation type
  public void test090() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Target;\n" +
        "import java.lang.annotation.ElementType;\n" +
        "\n" +
        "public class X {\n" +
        "\n" +
        "    @Target(ElementType.TYPE) @interface Annot1 {}\n" +
        "    \n" +
        "    @Annot1 @interface Annot2 {}\n" +
        "}"
    },
        "");
  }

  // Add check that a field cannot have an annotation targetting TYPE
  public void test091() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Target;\n" +
        "import java.lang.annotation.ElementType;\n" +
        "\n" +
        "public class X {\n" +
        "\n" +
        "    @Target(ElementType.TYPE) @interface Marker {}\n" +
        "    \n" +
        "    @Marker static int i = 123;\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 8)\n" +
        "	@Marker static int i = 123;\n" +
        "	^^^^^^^\n" +
        "The annotation @X.Marker is disallowed for this location\n" +
        "----------\n");
  }

  // Add check that a field cannot have an annotation targetting FIELD
  public void test092() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Target;\n" +
        "import java.lang.annotation.ElementType;\n" +
        "\n" +
        "public class X {\n" +
        "\n" +
        "    @Target(ElementType.FIELD) @interface Marker {}\n" +
        "    \n" +
        "    @Marker static int i = 123;\n" +
        "}"
    },
        "");
  }

  // @Inherited can only be used on annotation types
  public void test093() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Inherited;\n" +
        "\n" +
        "@Deprecated\n" +
        "@Inherited\n" +
        "class A {\n" +
        "}\n" +
        "\n" +
        "class B extends A {\n" +
        "}\n" +
        "\n" +
        "class C extends B {\n" +
        "}\n" +
        "\n" +
        "public class X {\n" +
        "	C c;\n" +
        "}\n"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 4)\r\n" +
        "	@Inherited\r\n" +
        "	^^^^^^^^^^\n" +
        "The annotation @Inherited is disallowed for this location\n" +
        "----------\n");
  }

  // check handling of empty array initializer (binary check)
  public void test094() {
    this.runConformTest(
        new String[] {
        "I.java",
        "import java.lang.annotation.Target;\n" +
        "\n" +
        "@Target({}) @interface I {}",
    },
        "");
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@I public class X {}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 1)\n" +
        "	@I public class X {}\n" +
        "	^^\n" +
        "The annotation @I is disallowed for this location\n" +
        "----------\n",
        null,
        false,
        null);
  }

  // check no interaction between Retention and Target (switch fall-thru)
  public void test095() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.*;\n" +
        "\n" +
        "@Retention(RetentionPolicy.RUNTIME)\n" +
        "@interface Ann {}\n" +
        "\n" +
        "public class X {\n" +
        "	@Ann\n" +
        "	void foo() {}\n" +
        "}\n",
    },
        "");
  }

  // check attributes for parameters
  public void test096() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import static java.lang.annotation.RetentionPolicy.*;\n" +
        "import java.lang.annotation.Retention;\n" +
        "import java.lang.annotation.Annotation;\n" +
        "import java.lang.reflect.Method;\n" +
        "\n" +
        "@Retention(CLASS) @interface Attr {\n" +
        "}\n" +
        "\n" +
        "@Retention(RUNTIME) @interface Foo {\n" +
        "	int id() default 0;\n" +
        "}\n" +
        "@Foo(id=5) @Attr public class X {\n" +
        "	public void foo(@Foo(id=5) @Attr final int j, @Attr final int k, int n) {\n" +
        "	}\n" +
        "	\n" +
        "	public static void main(String[] args) {\n" +
        "		try {\n" +
        "			Class c = X.class;\n" +
        "			Annotation[] annots = c.getAnnotations();\n" +
        "			System.out.print(annots.length);\n" + "			Method method = c.getMethod(\"foo\", Integer.TYPE, Integer.TYPE, Integer.TYPE);\n" +
        "			Annotation[][] annotations = method.getParameterAnnotations();\n" +
        "			final int length = annotations.length;\n" +
        "			System.out.print(length);\n" +
        "			if (length == 3) {\n" +
        "				System.out.print(annotations[0].length);\n" +
        "				System.out.print(annotations[1].length);\n" +
        "				System.out.print(annotations[2].length);\n" +
        "			}\n" +
        "		} catch(NoSuchMethodException e) {\n" +
        "		}\n" +
        "	}\n" +
        "}",
    },
        "13100");
  }

  public void test097() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "	int id default 0;\n" +
        "}\n" +
        "\n" +
        "@I() public class X {\n" +
        "	public static void main(String[] s) {\n" +
        "		System.out.println(X.class.getAnnotation(I.class));\n" +
        "	}\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	int id default 0;\n" +
        "	       ^^^^^^^\n" +
        "Syntax error on token \"default\", = expected\n" +
        "----------\n");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80328
  public void test098() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "@interface I {\n" +
        "	int id default 0;\n" +
        "}\n" +
        "\n" +
        "@I() public class X {\n" +
        "	public static void main(String[] s) {\n" +
        "		System.out.println(X.class.getAnnotation(I.class));\n" +
        "	}\n" +
        "}"
    },
        "----------\n" +
        "1. ERROR in X.java (at line 2)\n" +
        "	int id default 0;\n" +
        "	       ^^^^^^^\n" +
        "Syntax error on token \"default\", = expected\n" +
        "----------\n");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80780
  public void test099() {
    this.runConformTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.*;\n" +
        "import java.lang.reflect.Method;\n" +
        "\n" +
        "public class X {\n" +
        "    public static void main(String[] args) {\n" +
        "        Object o = new X();\n" +
        "        for (Method m : o.getClass().getMethods()) {\n" +
        "            if (m.isAnnotationPresent(MyAnon.class)) {\n" +
        "                System.out.println(m.getAnnotation(MyAnon.class).c());\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "    @MyAnon(c = X.class) \n" +
        "    public void foo() {}\n" +
        "\n" +
        "    @Retention(RetentionPolicy.RUNTIME) \n" +
        "    public @interface MyAnon {\n" +
        "        Class c();\n" +
        "    }\n" +
        "    public interface I {\n" +
        "    }\n" +
        "}"
    },
        "class X");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80544
  public void test100() {
    this.runConformTest(
        new String[] {
        "X.java",
        "abstract class Foo {\n" +
        "	abstract protected boolean accept(Object o);\n" +
        "}\n" +
        "\n" +
        "public class X extends Foo {\n" +
        "	@Override \n" +
        "	protected boolean accept(Object o) { return false; }\n" +
        "}\n",
    },
        "");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=81148
  public void test101() {
    this.runNegativeTest(
        new String[] {
        "X.java",
        "import java.lang.annotation.Target;\n" +
        "\n" +
        "@Target(Element)\n" +
        "public @interface X {\n" +
        "	\n" +
        "	boolean UML() default false;\n" +
        "	boolean platformDependent() default true;\n" +
        "	boolean OSDependent() default true;\n" +
        "}\n",
    },
        "----------\n" +
        "1. ERROR in X.java (at line 3)\r\n" +
        "	@Target(Element)\r\n" +
        "	        ^^^^^^^\n" +
        "Element cannot be resolved\n" +
        "----------\n");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80964
  public void test102() {
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "  @TestAnnotation(testAttribute = \"test\") class A {\n" +
        "  }\n" +
        "  public static void main(String[] args) {\n" +
        "    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));\n" +
        "  }\n" +
        "}",
        "TestAnnotation.java",
        "import java.lang.annotation.ElementType;\n" +
        "import java.lang.annotation.RetentionPolicy;\n" +
        "import java.lang.annotation.Target;\n" +
        "import java.lang.annotation.Retention;\n" +
        "@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface\n" +
        "TestAnnotation {\n" +
        "    String testAttribute();\n" +
        "}\n"
    },
        "true");
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=80964
  public void test103() {
    this.runConformTest(
        new String[] {
        "TestAnnotation.java",
        "import java.lang.annotation.ElementType;\n" +
        "import java.lang.annotation.RetentionPolicy;\n" +
        "import java.lang.annotation.Target;\n" +
        "import java.lang.annotation.Retention;\n" +
        "@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface\n" +
        "TestAnnotation {\n" +
        "    String testAttribute();\n" +
        "}\n"
    },
        "");
    this.runConformTest(
        new String[] {
        "X.java",
        "public class X {\n" +
        "  @TestAnnotation(testAttribute = \"test\") class A {\n" +
        "  }\n" +
        "  public static void main(String[] args) {\n" +
        "    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));\n" +
        "  }\n" +
        "}",
    },
        "true",
        null,
        false,
        null);
  }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81825
	public void test104() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface ValuesAnnotation {\n" +
				"	byte[] byteArrayValue();\n" +
				"	char[] charArrayValue();\n" +
				"	boolean[] booleanArrayValue();\n" +
				"	int[] intArrayValue();\n" +
				"	short[] shortArrayValue();\n" +
				"	long[] longArrayValue();\n" +
				"	float[] floatArrayValue();\n" +
				"	double[] doubleArrayValue();\n" +
				"	String[] stringArrayValue();\n" +
				"	ValuesEnum[] enumArrayValue();\n" +
				"	ValueAttrAnnotation[] annotationArrayValue();\n" +
				"	Class[] classArrayValue();\n" +
				"	byte byteValue();\n" +
				"	char charValue();\n" +
				"	boolean booleanValue();\n" +
				"	int intValue();\n" +
				"	short shortValue();\n" +
				"	long longValue();\n" +
				"	float floatValue();\n" +
				"	double doubleValue();\n" +
				"	String stringValue();\n" +
				"	ValuesEnum enumValue();\n" +
				"	ValueAttrAnnotation annotationValue();\n" +
				"	Class classValue();\n" +
				"}\n" +
				"enum ValuesEnum {\n" +
				"	ONE, TWO;\n" +
				"}\n" +
				"\n" +
				"@interface ValueAttrAnnotation {\n" +
				"	String value() default \"\";\n" +
				"}\n" +
				"@interface ValueAttrAnnotation1 {\n" +
				"	String value();\n" +
				"}\n" +
				"@interface ValueAttrAnnotation2 {\n" +
				"	String value();\n" +
				"}\n" +
				"@ValuesAnnotation(\n" +
				"  byteValue = 1,\n" +
				"  charValue = \'A\',\n" +
				"  booleanValue = true,\n" +
				"  intValue = 1,\n" +
				"  shortValue = 1,\n" +
				"  longValue = 1L,\n" +
				"  floatValue = 1.0f,\n" +
				"  doubleValue = 1.0d,\n" +
				"  stringValue = \"A\",\n" +
				"\n" +
				"  enumValue = ValuesEnum.ONE,\n" +
				"  annotationValue = @ValueAttrAnnotation( \"annotation\"),\n" +
				"  classValue = X.class,\n" +
				"\n" +
				"  byteArrayValue = { 1, -1},\n" +
				"  charArrayValue = { \'c\', \'b\', (char)-1},\n" +
				"  booleanArrayValue = {true, false},\n" +
				"  intArrayValue = { 1, -1},\n" +
				"  shortArrayValue = { (short)1, (short)-1},\n" +
				"  longArrayValue = { 1L, -1L},\n" +
				"  floatArrayValue = { 1.0f, -1.0f},\n" +
				"  doubleArrayValue = { 1.0d, -1.0d},\n" +
				"  stringArrayValue = { \"aa\", \"bb\"},\n" +
				"\n" +
				"  enumArrayValue = {ValuesEnum.ONE, ValuesEnum.TWO},\n" +
				"  annotationArrayValue = {@ValueAttrAnnotation( \"annotation1\"),\n" +
				"@ValueAttrAnnotation( \"annotation2\")},\n" +
				"  classArrayValue = {X.class, X.class}\n" +
				")\n" +
				"@ValueAttrAnnotation1( \"classAnnotation1\")\n" +
				"@ValueAttrAnnotation2( \"classAnnotation2\")\n" +
				"public class X {\n" +
				"\n" +
				"  @ValueAttrAnnotation1( \"fieldAnnotation1\")\n" +
				"  @ValueAttrAnnotation2( \"fieldAnnotation2\")\n" +
				"  public String testfield = \"test\";\n" +
				"\n" +
				"  @ValueAttrAnnotation1( \"methodAnnotation1\")\n" +
				"  @ValueAttrAnnotation2( \"methodAnnotation2\")\n" +
				"  @ValueAttrAnnotation()\n" +
				"  public void testMethod( \n" +
				"      @ValueAttrAnnotation1( \"param1Annotation1\") \n" +
				"      @ValueAttrAnnotation2( \"param1Annotation2\") String param1, \n" +
				"      @ValueAttrAnnotation1( \"param2Annotation1\") \n" +
				"      @ValueAttrAnnotation2( \"param2Annotation2\") int param2) {\n" +
				"    // @ValueAttrAnnotation( \"codeAnnotation\")\n" +
				"  }\n" +
				"}\n"
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82136
	public void test105() {
		this.runConformTest(
			new String[] {
				"Property.java",
				"import java.lang.annotation.Documented;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"\n" +
				"@Documented\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"public @interface Property\n" +
				"{\n" +
				"  String property();\n" +
				"  String identifier() default \"\";\n" +
				"}",
				"Properties.java",
				"import java.lang.annotation.Documented;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"\n" +
				"@Documented\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"public @interface Properties {\n" +
				"  Property[] value();\n" +
				"}",
				"X.java",
				"@Properties({\n" +
				"  @Property(property = \"prop\", identifier = \"someIdentifier\"),\n" +
				"  @Property(property = \"type\")\n" +
				"})\n" +
				"public interface X {\n" +
				"  void setName();\n" +
				"  String getName();\n" +
				"}"
			},
			"");
/*			try {
				byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
				new ClassFileReader(classFileBytes, "X.java".toCharArray(), true);
			} catch (ClassFormatException e) {
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				assertTrue("IOException", false);
			}*/
	}

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939
    public void test106() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "public @interface X {\n" +
                "    int[] bar() default null;\n" +
                "}",
            },
            "----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int[] bar() default null;\n" +
			"	                    ^^^^\n" +
			"The value for annotation attribute X.bar must be a constant expression\n" +
			"----------\n");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939
    public void test107() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@interface Ann {\n" +
                "    int[] bar();\n" +
                "}\n" +
                "@Ann(bar=null) class X {}",
            },
            "----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	@Ann(bar=null) class X {}\n" +
			"	         ^^^^\n" +
			"The value for annotation attribute Ann.bar must be a constant expression\n" +
			"----------\n");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939 - variation
    public void test108() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Foo {}\n" +
				"\n" +
				"@interface Bar {\n" +
				"    Foo[] foo() default null;\n" +
				"}\n" +
				"\n" +
				"@Bar(foo=null)\n" +
				"public class X { \n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Foo[] foo() default null;\n" +
			"	                    ^^^^\n" +
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	@Bar(foo=null)\n" +
			"	         ^^^^\n" +
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" +
			"----------\n");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939 - variation
    public void test109() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Foo {}\n" +
				"\n" +
				"@interface Bar {\n" +
				"    Foo[] foo() default \"\";\n" +
				"}\n" +
				"\n" +
				"@Bar(foo=\"\")\n" +
				"public class X { \n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Foo[] foo() default \"\";\n" +
			"	                    ^^\n" +
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	@Bar(foo=\"\")\n" +
			"	         ^^\n" +
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" +
			"----------\n");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84791
    public void test110() {
        this.runConformTest(
            new String[] {
                "X.java",
				"import java.lang.annotation.Annotation;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"@interface Ann {\n" +
				"}\n" +
				"\n" +
				"interface Iface extends Ann {\n" +
				"}\n" +
				"\n" +
				"abstract class Klass implements Ann {\n" +
				"}\n" +
				"\n" +
				"class SubKlass extends Klass {\n" +
				"	public Class<? extends Annotation> annotationType() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Class c = SubKlass.class;\n" +
				"		System.out.print(\"Classes:\");\n" +
				"		while (c != Object.class) {\n" +
				"			System.out.print(\"-> \" + c.getName());\n" +
				"			c = c.getSuperclass();\n" +
				"		}\n" +
				"\n" +
				"		System.out.print(\", Interfaces:\");\n" +
				"		c = SubKlass.class;\n" +
				"		while (c != Object.class) {\n" +
				"			Class[] i = c.getInterfaces();\n" +
				"			System.out.print(\"-> \" + Arrays.asList(i));\n" +
				"			c = c.getSuperclass();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
            },
			"Classes:-> SubKlass-> Klass, Interfaces:-> []-> [interface Ann]");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84791 - variation
    public void test111() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"import java.lang.annotation.Annotation;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"@interface Ann {\n" +
				"	int foo();\n" +
				"}\n" +
				"\n" +
				"interface Iface extends Ann {\n" +
				"}\n" +
				"\n" +
				"abstract class Klass implements Ann {\n" +
				"}\n" +
				"\n" +
				"class SubKlass extends Klass {\n" +
				"	public Class<? extends Annotation> annotationType() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"class AnnImpl implements Ann {\n" +
				"    public boolean equals(Object obj) { return false; }\n" +
				"    public int hashCode() { return 0; }\n" +
				"    public String toString() { return null; }\n" +
				"    public Class<? extends Annotation> annotationType() { return null; }\n" +
				"    public int foo() { return 0; }\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Class c = SubKlass.class;\n" +
				"		System.out.println(\"Classes:\");\n" +
				"		while (c != Object.class) {\n" +
				"			System.out.println(\"-> \" + c.getName());\n" +
				"			c = c.getSuperclass();\n" +
				"		}\n" +
				"\n" +
				"		System.out.println();\n" +
				"		System.out.println(\"Interfaces:\");\n" +
				"		c = SubKlass.class;\n" +
				"		while (c != Object.class) {\n" +
				"			Class[] i = c.getInterfaces();\n" +
				"			System.out.println(\"-> \" + Arrays.asList(i));\n" +
				"			c = c.getSuperclass();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 14)\n" +
			"	class SubKlass extends Klass {\n" +
			"	      ^^^^^^^^\n" +
			"The type SubKlass must implement the inherited abstract method Ann.foo()\n" +
			"----------\n");
    }

  public void testRefactoringsOnAnnotations() {
    String initialProjectContent = "package testPackage;\n" +
            "\n" +
            "@interface Author {\n" +
            "String value() default \"unknown\";\n" +
            "}\n" +
            "\n" +
            "@interface Copyright {\n" +
            "Author value() default @Author();\n" +
            "}\n" +
    		"\n" +
            "@Copyright class X1 {\n" +
            "}\n" +
    		"\n" +
            "@Copyright() class X2 {\n" +
            "}\n" +
    		"\n" +
            "@Copyright(@Author()) class X3 {\n" +
            "}\n" +
    		"\n" +
            "@Copyright(@Author(\"Man\")) class X4 {\n" +
            "}\n" +
            "\n" +
            "@Copyright(@Author(value = \"Man\")) class X5 {\n" +
            "}\n" +
            "\n" +
            "@Copyright(value = @Author()) class X6 {\n" +
            "}\n" +
            "\n" +
            "@Copyright(value = @Author(\"John\")) class X7 {\n" +
            "}\n" +
            "\n" +
            "@Copyright(value = @Author(value = \"Bill\")) class X8 {\n" +
            "}\n" +
            "\n" +
            "class X9 {\n" +
            "@Copyright(@Author(\"Mary, hihi\"))public static int field;\n" +
            "}\n" +
            "\n" +
            "class X10 {\n" +
            "@Copyright(@Author(\"Mary, hihi\")) public static void xxx() {\n" +
            "   @Copyright(@Author(\"Mary, hihi\")) int s;\n" +
            "}\n" +
            "}\n";

    String expectedProjectContent = "package testPackage;\n" +
            "\n" +
            "@interface Writer {\n" +
            "String value() default \"unknown\";\n" +
            "}\n" +
            "\n" +
            "@interface Copyright {\n" +
            "Writer value() default @Writer();\n" +
            "}\n" +
    		"\n" +
            "@Copyright class X1 {\n" +
            "}\n" +
    		"\n" +
            "@Copyright() class X2 {\n" +
            "}\n" +
    		"\n" +
            "@Copyright(@Writer()) class X3 {\n" +
            "}\n" +
    		"\n" +
            "@Copyright(@Writer(\"Man\")) class X4 {\n" +
            "}\n" +
            "\n" +
            "@Copyright(@Writer(value = \"Man\")) class X5 {\n" +
            "}\n" +
            "\n" +
            "@Copyright(value = @Writer()) class X6 {\n" +
            "}\n" +
            "\n" +
            "@Copyright(value = @Writer(\"John\")) class X7 {\n" +
            "}\n" +
            "\n" +
            "@Copyright(value = @Writer(value = \"Bill\")) class X8 {\n" +
            "}\n" +
            "\n" +
            "class X9 {\n" +
            "@Copyright(@Writer(\"Mary, hihi\"))public static int field;\n" +
            "}\n" +
            "\n" +
            "class X10 {\n" +
            "@Copyright(@Writer(\"Mary, hihi\")) public static void xxx() {\n" +
            "   @Copyright(@Writer(\"Mary, hihi\")) int s;\n" +
            "}\n" +
            "}\n";

    Project initialProject = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
            initialProjectContent,
        "X.java", "testPackage"
        )});

    Project expectedProject1 = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
            expectedProjectContent,
        "X.java", "testPackage"
        )});

    Project expectedProject2 = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
            initialProjectContent,
        "X.java", "testPackage"
        )});


    RenameType renameType = new RenameType(new NullContext(initialProject),
        initialProject.getTypeRefForName("testPackage.Author").getBinCIType());
    renameType.setNewName("Writer");

    RwRefactoringTestUtils.assertRefactoring(renameType, initialProject, expectedProject1);

    try {
      initialProject.getProjectLoader().build();
    } catch (Exception e) {
      e.printStackTrace();
    }


    RenameType renameType2 = new RenameType(new NullContext(initialProject),
        initialProject.getTypeRefForName("testPackage.Writer").getBinCIType());
    renameType2.setNewName("Author");

    RwRefactoringTestUtils.assertRefactoring(renameType2, initialProject, expectedProject2);
  }

}
