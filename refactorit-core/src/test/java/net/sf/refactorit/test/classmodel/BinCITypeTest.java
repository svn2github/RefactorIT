/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.classmodel;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test driver for {@link BinCIType}.
 */
public class BinCITypeTest extends TestCase {
  /** Logger instance. */
  static final Category cat =
      Category.getInstance(BinCITypeTest.class.getName());

  /** Creates new BinCITypeTest */
  public BinCITypeTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(BinCITypeTest.class);
    suite.setName("BinCIType tests");

    suite.addTest(BinClassTest.suite());
    suite.addTest(BinInterfaceTest.suite());
    suite.addTest(GetBodyASTTest.suite());
    suite.addTest(LocationAwareTest.suite());
    return suite;
  }

  /**
   * Asserts that method equals expected value.
   *
   * @param message message to show in case assertion fails.
   * @param expected expected method.
   * @param actual actual method.
   */
  protected static void assertMethodEquals(String message,
      BinMethod expected,
      BinMethod actual) {
    if (!expected.getName().equals(actual.getName())) {
      fail(message + ". Expected: " + expected.getQualifiedNameWithParamTypes()
          + ", actual: " + actual.getQualifiedNameWithParamTypes()
          + ". Names don't match");
    }

    if (expected.getModifiers() != actual.getModifiers()) {
      fail(message + ". Expected: " + expected.getQualifiedNameWithParamTypes()
          + ", actual: " + actual.getQualifiedNameWithParamTypes()
          + ". Modifiers don't match");
    }

    if (!expected.getReturnType().getQualifiedName().equals(
        actual.getReturnType().getQualifiedName())) {
      fail(message + ". Expected: " + expected.getQualifiedNameWithParamTypes()
          + ", actual: " + actual.getQualifiedNameWithParamTypes()
          + ". Return types don't match");
    }

    final BinParameter expectedParameters[] = expected.getParameters();
    final BinParameter actualParameters[] = expected.getParameters();
    if (expectedParameters.length != actualParameters.length) {
      fail(message + ". Expected: " + expected.getQualifiedNameWithParamTypes()
          + ", actual: " + actual.getQualifiedNameWithParamTypes()
          + ". Number of parameters doesn't match");
    }
    for (int i = 0; i < expectedParameters.length; i++) {
      final BinParameter expectedParameter = expectedParameters[i];
      final BinParameter actualParameter = actualParameters[i];
      if (!expectedParameter.getTypeRef().getQualifiedName().equals(
          actualParameter.getTypeRef().getQualifiedName())) {
        fail(message + ". Expected: "
            + expected.getQualifiedNameWithParamTypes()
            + ", actual: " + actual.getQualifiedNameWithParamTypes()
            + ". Type of parameter #" + (i + 1) + " doesn't match");
      }

      if (expectedParameter.getModifiers() != actualParameter.getModifiers()) {
        fail(message + ". Expected: "
            + expected.getQualifiedNameWithParamTypes()
            + ", actual: " + actual.getQualifiedNameWithParamTypes()
            + ". Modifiers of parameter #" + (i + 1) + " don't match");
      }
    }

    final String expectedOwner = expected.getOwner().getQualifiedName();
    final String actualOwner = actual.getOwner().getQualifiedName();
    if (!expectedOwner.equals(actualOwner)) {
      fail(message + ". Owners don't match. Expected: " + expectedOwner
          + ", actual: " + actualOwner);
    }
  }

  /**
   * Finds method by name from the provided list of methods.
   *
   * @param methods list of methods.
   * @param name method name.
   *
   * @return method. Never returns <code>null</code>.
   */
  protected static BinMethod findMethod(BinMethod[] methods, String name) {
    for (int i = 0; i < methods.length; i++) {
      final BinMethod method = methods[i];
      if (name.equals(method.getName())) {
        return method;
      }
    }

    fail("Method " + name + " not found");
    throw new RuntimeException("This should never be reached");
  }

  /**
   * Gets test project.
   *
   * @return project. Neve returns <code>null</code>.
   */
  protected static Project getTestProject() throws Exception {
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("getDeclaredMethods"));

    return project;
  }

  /**
   * Tests location awareness of {@link BinCIType}.
   */
  public static class LocationAwareTest extends TestCase {
    private Project project;

    public LocationAwareTest(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(LocationAwareTest.class);
      suite.setName("Location awareness");
      return suite;
    }

    protected void setUp() throws Exception {
      project =
          Utils.createTestRbProject(
          Utils.getTestProjects().getProject("LocationAware"));
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests location of Test class.
     */
    public void testTest() {
      cat.info("Testing location of Test class");
      final LocationAware type =
          project.getTypeRefForName("Test").getBinCIType();
      assertEquals("Source file",
          "Test.java",
          type.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 3, type.getStartLine());
      assertEquals("Start column", 1, type.getStartColumn());
      assertEquals("End line", 29, type.getEndLine());
      assertEquals("End column", 2, type.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Test.main anonymous Runnable class.
     */
    public void testTestMainAnonymousRunnable() {
      cat.info("Testing location of Test.main anonymous Runnable class");
      final BinCIType test =
          project.getTypeRefForName("Test").getBinCIType();
      final BinMethod main = test.getDeclaredMethods()[0];
      assertEquals("Name of main method", "main", main.getName());

      // Get first type declared inside main method body
      final LocationAware type = (LocationAware) main.getDeclaredTypes().get(0);
      assertEquals("Source file", "Test.java",
          type.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 20, type.getStartLine());
      assertEquals("Start column", 7, type.getStartColumn());
      assertEquals("End line", 24, type.getEndLine());
      assertEquals("End column", 7, type.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Test.main params parameter.
     */
    public void testTestMainParams() {
      cat.info("Testing location of Test.main params parameter");
      final BinCIType test =
          project.getTypeRefForName("Test").getBinCIType();
      final BinMethod main = test.getDeclaredMethods()[0];
      assertEquals("Name of main method", "main", main.getName());
      final LocationAware parameter = main.getParameters()[0];
      assertTrue("parameter params found", parameter != null);

      assertEquals("Source file",
          "Test.java",
          parameter.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 11, parameter.getStartLine());
      assertEquals("Start column", 33, parameter.getStartColumn());
      assertEquals("End line", 11, parameter.getEndLine());
      assertEquals("End column", 48, parameter.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Test.main tmp local variable.
     */
    public void testTestMainTmpLocal() {
      cat.info("Testing location of Test.main tmp local variable");
      final BinCIType test =
          project.getTypeRefForName("Test").getBinCIType();
      final LocationAware local = ItemByNameFinder.findLocalVariable(test, "tmp");
      assertTrue("local variable tmp found", local != null);

      assertEquals("Source file",
          "Test.java",
          local.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 12, local.getStartLine());
      assertEquals("Start column", 5, local.getStartColumn());
      assertEquals("End line", 12, local.getEndLine());
      assertEquals("End column", 40, local.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Test static initializer.
     */
    public void testTestStaticInitializer() {
      cat.info("Testing location of Test static initializer");
      final BinClass test =
          (BinClass) project.getTypeRefForName("Test")
          .getBinCIType();
      final LocationAware initializer = test.getInitializers()[0];

      assertEquals("Source file",
          "Test.java",
          initializer.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 4, initializer.getStartLine());
      assertEquals("Start column", 3, initializer.getStartColumn());
      assertEquals("End line", 6, initializer.getEndLine());
      assertEquals("End column", 4, initializer.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Test instance initializer.
     */
    public void testTestInstanceInitializer() {
      cat.info("Testing location of Test instance initializer");
      final BinClass test =
          (BinClass) project.getTypeRefForName("Test")
          .getBinCIType();
      final LocationAware initializer = test.getInitializers()[1];

      assertEquals("Source file",
          "Test.java",
          initializer.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 8, initializer.getStartLine());
      assertEquals("Start column", 3, initializer.getStartColumn());
      assertEquals("End line", 10, initializer.getEndLine());
      assertEquals("End column", 4, initializer.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Test.InnerInterface interface.
     */
    public void testTestInnerInterface() {
      cat.info("Testing location of Test.InnerInterface interface");
      final LocationAware type =
          project.getTypeRefForName("Test$InnerInterface")
          .getBinCIType();
      assertEquals("Source file",
          "Test.java",
          type.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 27, type.getStartLine());
      assertEquals("Start column", 3, type.getStartColumn());
      assertEquals("End line", 28, type.getEndLine());
      assertEquals("End column", 5, type.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Test3 class.
     */
    public void testTest3() {
      cat.info("Testing location of Test3 class");
      final LocationAware type =
          project.getTypeRefForName("Test3").getBinCIType();
      assertEquals("Source file",
          "Test.java",
          type.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 31, type.getStartLine());
      assertEquals("Start column", 1, type.getStartColumn());
      assertEquals("End line", 49, type.getEndLine());
      assertEquals("End column", 2, type.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Test3.Inner class.
     */
    public void testTest3Inner() {
      cat.info("Testing location of Test3.Inner class");
      final LocationAware type =
          project.getTypeRefForName("Test3$Inner")
          .getBinCIType();
      assertEquals("Source file",
          "Test.java",
          type.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 34, type.getStartLine());
      assertEquals("Start column", 3, type.getStartColumn());
      assertEquals("End line", 39, type.getEndLine());
      assertEquals("End column", 4, type.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Test3.Inner.B class.
     */
    public void testTest3InnerB() {
      cat.info("Testing location of Test3.Inner.B class");
      final LocationAware type =
          project.getTypeRefForName("Test3$Inner$B")
          .getBinCIType();
      assertEquals("Source file",
          "Test.java",
          type.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 37, type.getStartLine());
      assertEquals("Start column", 5, type.getStartColumn());
      assertEquals("End line", 38, type.getEndLine());
      assertEquals("End column", 14, type.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Test3.test method inner class A.
     */
    public void testTest3TestA() {
      cat.info("Testing location of Test3.test method innver class A");
      final BinCIType test3 =
          project.getTypeRefForName("Test3").getBinCIType();
      final BinMethod test = test3.getDeclaredMethods()[1];
      assertEquals("Name of test method", "test", test.getName());

      // Get first type declared inside test method body
      final LocationAware type = (LocationAware) test.getDeclaredTypes().get(0);
      assertEquals("Source file", "Test.java",
          type.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 42, type.getStartLine());
      assertEquals("Start column", 5, type.getStartColumn());
      assertEquals("End line", 44, type.getEndLine());
      assertEquals("End column", 6, type.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Hello interface.
     */
    public void testHello() {
      cat.info("Testing location of Hello interface");
      final LocationAware type =
          project.getTypeRefForName("Hello").getBinCIType();
      assertEquals("Source file",
          "Test.java",
          type.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 51, type.getStartLine());
      assertEquals("Start column", 1, type.getStartColumn());
      assertEquals("End line", 54, type.getEndLine());
      assertEquals("End column", 4, type.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Hello.a field.
     */
    public void testHelloA() {
      cat.info("Testing location of Hello.a field");
      final BinCIType type =
          project.getTypeRefForName("Hello").getBinCIType();
      final LocationAware field = type.getDeclaredFields()[0];
      assertEquals("Source file",
          "Test.java",
          field.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 52, field.getStartLine());
      assertEquals("Start column", 3, field.getStartColumn());
      assertEquals("End line", 52, field.getEndLine());
      assertEquals("End column", 16, field.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Hello.c field.
     */
    public void testHelloC() {
      cat.info("Testing location of Hello.c field");
      final BinCIType type =
          project.getTypeRefForName("Hello").getBinCIType();
      final LocationAware field = type.getDeclaredFields()[1];
      assertEquals("Source file",
          "Test.java",
          field.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 52, field.getStartLine());
      assertEquals("Start column", 3, field.getStartColumn());
      assertEquals("End line", 52, field.getEndLine());
      assertEquals("End column", 16, field.getEndColumn()); // according to spec location aware covers the whole declaration
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Hello.b method.
     */
    public void testHelloB() {
      cat.info("Testing location of Hello.b method");
      final BinCIType type =
          project.getTypeRefForName("Hello").getBinCIType();
      final LocationAware method = type.getDeclaredMethods()[0];
      assertEquals("Source file",
          "Test.java",
          method.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 53, method.getStartLine());
      assertEquals("Start column", 3, method.getStartColumn());
      assertEquals("End line", 53, method.getEndLine());
      assertEquals("End column", 12, method.getEndColumn());
      cat.info("SUCCESS");
    }

    /**
     * Tests location of Point constructor.
     */
    public void testPointConstructor() {
      cat.info("Testing location of Point constructor");
      final BinClass type =
          (BinClass) project.getTypeRefForName("Point")
          .getBinCIType();
      final LocationAware ctor = type.getDeclaredConstructors()[0];
      assertEquals("Source file",
          "Test.java",
          ctor.getCompilationUnit().getSource().getRelativePath());
      assertEquals("Start line", 58, ctor.getStartLine());
      assertEquals("Start column", 3, ctor.getStartColumn());
      assertEquals("End line", 58, ctor.getEndLine());
      assertEquals("End column", 50, ctor.getEndColumn());
      cat.info("SUCCESS");
    }
  }


  /**
   * Test driver for {@link BinCIType#getBodyAST}.
   */
  public static class GetBodyASTTest extends TestCase {
    /** Logger instance. */
    private static final Category cat =
        Category.getInstance(GetBodyASTTest.class);

    private Project project;

    public GetBodyASTTest(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(GetBodyASTTest.class);
      suite.setName("getBodyAST");
      return suite;
    }

    protected void setUp() throws Exception {
      project =
          Utils.createTestRbProject(
          Utils.getTestProjects().getProject("LocationAware"));
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests getBodyAST on class Test.
     */
    public void testGetBodyASTClassTest() throws Exception {
      cat.info("Testing getBodyAST on class Test");

      final BinCIType test =
          project.getTypeRefForName("Test").getBinCIType();
      final ASTImpl bodyAst = test.getBodyAST();
      assertTrue("getBodyAST() != null", bodyAst != null);
      assertEquals("Body start line", 3, bodyAst.getStartLine());
      assertEquals("Body start column", 19, bodyAst.getStartColumn());
      assertEquals("Body end line", 29, bodyAst.getEndLine());
      assertEquals("Body end column", 2, bodyAst.getEndColumn());

      cat.info("SUCCESS");
    }

    /**
     * Tests getBodyAST on interface Test.InnerInterface.
     */
    public void testGetBodyASTInterfaceTestInnerInterface() throws Exception {
      cat.info("Testing getBodyAST on class Test");

      final BinCIType innerInterface =
          project.getTypeRefForName("Test$InnerInterface")
          .getBinCIType();
      final ASTImpl bodyAst = innerInterface.getBodyAST();
      assertTrue("getBodyAST() != null", bodyAst != null);
      assertEquals("Body start line", 27, bodyAst.getStartLine());
      assertEquals("Body start column", 28, bodyAst.getStartColumn());
      assertEquals("Body end line", 28, bodyAst.getEndLine());
      assertEquals("Body end column", 5, bodyAst.getEndColumn());

      cat.info("SUCCESS");
    }
  }


  /**
   * Test driver for {@link BinClass}.
   */
  public static class BinClassTest extends TestCase {
    /** Logger instance. */
    private static final Category cat =
        Category.getInstance(BinClassTest.class.getName());

    /** Creates new BinClassTest */
    public BinClassTest(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(BinClassTest.class);
      suite.setName("BinClass tests");
      return suite;
    }

    /** Tests getDeclaredMethods for class Test. */
    public void testGetDeclaredMethodsForTest() throws Exception {
      cat.info("Testing getDeclaredMethods for class Test");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef testRef = project.getTypeRefForName("Test");
      final BinClass test = (BinClass) testRef.getBinType();

      final BinMethod[] methods = test.getDeclaredMethods();
      assertEquals("Number of declared methods", 2, methods.length);

      final BinMethod methodMain =
          new BinMethod("main",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.STATIC
          | BinModifier.FINAL,
          BinMethod.Throws.NO_THROWS);
      methodMain.setOwner(testRef);
      assertMethodEquals("method main",
          methodMain,
          findMethod(methods, "main"));

      final BinMethod methodDumpMethods =
          new BinMethod("dumpMethods",
          new BinParameter[] {
          new BinParameter(
          "cls",
          project.getTypeRefForName("java.lang.Class"),
          0)
      }
          ,
          BinPrimitiveType.VOID_REF,
          BinModifier.PRIVATE | BinModifier.STATIC,
          BinMethod.Throws.NO_THROWS);
      methodDumpMethods.setOwner(testRef);
      assertMethodEquals("method dumpMethods",
          methodDumpMethods,
          findMethod(methods, "dumpMethods"));

      cat.info("SUCCESS");
    }

    /**
     * Tests getDeclaredMethods for class A.
     */
    public void testGetDeclaredMethodsForA() throws Exception {
      cat.info("Testing getDeclaredMethods for class A");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef aRef = project.getTypeRefForName("A");
      final BinClass a = (BinClass) aRef.getBinType();

      final BinMethod[] methods = a.getDeclaredMethods();
      assertEquals("Number of declared methods", 2, methods.length);

      final BinMethod methodA =
          new BinMethod("a",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC,
          BinMethod.Throws.NO_THROWS);
      methodA.setOwner(aRef);
      assertMethodEquals("method a",
          methodA,
          findMethod(methods, "a"));
      final BinMethod methodB =
          new BinMethod("b",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodB.setOwner(aRef);
      assertMethodEquals("method b",
          methodB,
          findMethod(methods, "b"));

      cat.info("SUCCESS");
    }

    /**
     * Tests getDeclaredMethods for class B.
     */
    public void testGetDeclaredMethodsForB() throws Exception {
      cat.info("Testing getDeclaredMethods for class B");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef bRef = project.getTypeRefForName("B");
      final BinClass b = (BinClass) bRef.getBinType();

      final BinMethod[] methods = b.getDeclaredMethods();
      assertEquals("Number of declared methods", 1, methods.length);

      final BinMethod methodC =
          new BinMethod("c",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodC.setOwner(bRef);
      assertMethodEquals("method c",
          methodC,
          findMethod(methods, "c"));

      cat.info("SUCCESS");
    }

    /**
     * Tests getDeclaredMethods for class C.
     */
    public void testGetDeclaredMethodsForC() throws Exception {
      cat.info("Testing getDeclaredMethods for class C");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef cRef = project.getTypeRefForName("C");
      final BinClass c = (BinClass) cRef.getBinType();

      final BinMethod[] methods = c.getDeclaredMethods();
      assertEquals("Number of declared methods", 2, methods.length);

      final BinMethod methodB =
          new BinMethod("b",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC,
          BinMethod.Throws.NO_THROWS);
      methodB.setOwner(cRef);
      assertMethodEquals("method b",
          methodB,
          findMethod(methods, "b"));

      final BinMethod methodC =
          new BinMethod("c",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC,
          BinMethod.Throws.NO_THROWS);
      methodC.setOwner(cRef);
      assertMethodEquals("method c",
          methodC,
          findMethod(methods, "c"));

      cat.info("SUCCESS");
    }

    /**
     * Tests getDeclaredMethods for class D.
     */
    public void testGetDeclaredMethodsForD() throws Exception {
      cat.info("Testing getDeclaredMethods for class D");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef dRef = project.getTypeRefForName("D");
      final BinClass d = (BinClass) dRef.getBinType();

      final BinMethod[] methods = d.getDeclaredMethods();
      assertEquals("Number of declared methods", 3, methods.length);

      final BinMethod methodA =
          new BinMethod("a",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodA.setOwner(dRef);
      assertMethodEquals("method a",
          methodA,
          findMethod(methods, "a"));

      final BinMethod methodB =
          new BinMethod("b",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodB.setOwner(dRef);
      assertMethodEquals("method b",
          methodB,
          findMethod(methods, "b"));

      final BinMethod methodC =
          new BinMethod("c",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodC.setOwner(dRef);
      assertMethodEquals("method c",
          methodC,
          findMethod(methods, "c"));

      cat.info("SUCCESS");
    }

    /**
     * Tests getDeclaredMethods for class G.
     */
    public void testGetDeclaredMethodsForG() throws Exception {
      cat.info("Testing getDeclaredMethods for class G");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef gRef = project.getTypeRefForName("G");
      final BinClass g = (BinClass) gRef.getBinType();

      final BinMethod[] methods = g.getDeclaredMethods();
      assertEquals("Number of declared methods", 0, methods.length);

      cat.info("SUCCESS");
    }

    /**
     * Tests getDeclaredMethods for class I.
     */
    public void testGetDeclaredMethodsForI() throws Exception {
      cat.info("Testing getDeclaredMethods for class I");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef iRef = project.getTypeRefForName("I");
      final BinClass i = (BinClass) iRef.getBinType();

      final BinMethod[] methods = i.getDeclaredMethods();
      assertEquals("Number of declared methods", 2, methods.length);

      final BinMethod methodA =
          new BinMethod("a",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodA.setOwner(iRef);
      assertMethodEquals("method a",
          methodA,
          findMethod(methods, "a"));

      final BinMethod methodB =
          new BinMethod("b",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodB.setOwner(iRef);
      assertMethodEquals("method b",
          methodB,
          findMethod(methods, "b"));

      cat.info("SUCCESS");
    }

    /**
     * Tests getConstructor()
     */
    public void testGetConstructor() throws Exception {
      cat.info("Testing getConstructor()");

      final Project project = Utils.createFakeProject();

      BinClass type = Utils.createClass("Test");

      type.setDeclaredConstructors(new BinConstructor[] {
          new BinConstructor(type.getTypeRef(),
          new BinParameter[] {
          new BinParameter("o", project.getObjectRef(), 0)},
          BinModifier.PUBLIC, BinMethod.Throws.NO_THROWS, true)});

      BinClass string = Utils.createClass("java.lang.String",
          project.getObjectRef().getBinCIType());

      BinConstructor cnstr = type.getAccessibleConstructor(type,
          new BinTypeRef[] {string.getTypeRef()});

      assertTrue("Found applicable constructor", cnstr != null);

      BinClass anotherType = Utils.createClass("Test2");

      cnstr = type.getAccessibleConstructor(
          anotherType,
          new BinTypeRef[] {string.getTypeRef()});

      assertTrue("Found accessible & applicable constructor", cnstr != null);

      cat.info("SUCCESS");
    }

  }


  /**
   * Test driver for {@link BinInterface}.
   */
  public static class BinInterfaceTest extends TestCase {
    /** Logger instance. */
    private static final Category cat =
        Category.getInstance(BinInterfaceTest.class.getName());

    /** Creates new BinClassTest */
    public BinInterfaceTest(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(BinInterfaceTest.class);
      suite.setName("BinInterface tests");
      return suite;
    }

    /** Tests getDeclaredMethods for interface InterfaceA. */
    public void testGetDeclaredMethodsForInterfaceA() throws Exception {
      cat.info("Testing getDeclaredMethods for interface InterfaceA");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef interfaceARef =
          project.getTypeRefForName("InterfaceA");
      final BinInterface interfaceA = (BinInterface) interfaceARef.getBinType();

      final BinMethod[] methods = interfaceA.getDeclaredMethods();
      assertEquals("Number of declared methods", 2, methods.length);

      final BinMethod methodA =
          new BinMethod("a",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodA.setOwner(interfaceARef);

      assertMethodEquals("method a",
          methodA,
          findMethod(methods, "a"));
      final BinMethod methodB =
          new BinMethod("b",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodB.setOwner(interfaceARef);
      assertMethodEquals("method b",
          methodB,
          findMethod(methods, "b"));

      cat.info("SUCCESS");
    }

    /** Tests getDeclaredMethods for interface InterfaceB. */
    public void testGetDeclaredMethodsForInterfaceB() throws Exception {
      cat.info("Testing getDeclaredMethods for interface InterfaceB");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef interfaceBRef =
          project.getTypeRefForName("InterfaceB");
      final BinInterface interfaceB = (BinInterface) interfaceBRef.getBinType();

      final BinMethod[] methods = interfaceB.getDeclaredMethods();
      assertEquals("Number of declared methods", 1, methods.length);

      final BinMethod methodC =
          new BinMethod("c",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodC.setOwner(interfaceBRef);
      assertMethodEquals("method c",
          methodC,
          findMethod(methods, "c"));

      cat.info("SUCCESS");
    }

    /**
     * Tests getAllAccessibleMethods for interface interfaceA.
     */
    public void testGetAllAccessibleMethodsForInterfaceA() throws Exception {
      cat.info("Testing getAllAccessibleMethods for interface interfaceA");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef interfaceARef =
          project.getTypeRefForName("InterfaceA");
      final BinInterface interfaceA = (BinInterface) interfaceARef.getBinType();

      final BinMethod[] methods = interfaceA.getAccessibleMethods(interfaceA);
      assertEquals("Number of accessible methods", 2, methods.length);

      final BinMethod methodA =
          new BinMethod("a",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodA.setOwner(interfaceARef);

      assertMethodEquals("method a",
          methodA,
          findMethod(methods, "a"));
      final BinMethod methodB =
          new BinMethod("b",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodB.setOwner(interfaceARef);
      assertMethodEquals("method b",
          methodB,
          findMethod(methods, "b"));

      cat.info("SUCCESS");
    }

    /**
     * Tests getAllAccessibleMethods for interface interfaceB.
     */
    public void testGetAllAccessibleMethodsForInterfaceB() throws Exception {
      cat.info("Testing getAllAccessibleMethods for interface interfaceB");

      final Project project = getTestProject();
      project.getProjectLoader().build();

      final BinTypeRef interfaceARef =
          project.getTypeRefForName("InterfaceA");
      final BinTypeRef interfaceBRef =
          project.getTypeRefForName("InterfaceB");
      final BinInterface interfaceB = (BinInterface) interfaceBRef.getBinType();

      final BinMethod[] methods = interfaceB.getAccessibleMethods(interfaceB);
      assertEquals("Number of accessible methods", 3, methods.length);

      final BinMethod methodA =
          new BinMethod("a",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodA.setOwner(interfaceARef);

      assertMethodEquals("method a",
          methodA,
          findMethod(methods, "a"));
      final BinMethod methodB =
          new BinMethod("b",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodB.setOwner(interfaceARef);
      assertMethodEquals("method b",
          methodB,
          findMethod(methods, "b"));

      final BinMethod methodC =
          new BinMethod("c",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          BinModifier.PUBLIC | BinModifier.ABSTRACT,
          BinMethod.Throws.NO_THROWS);
      methodC.setOwner(interfaceBRef);
      assertMethodEquals("method c",
          methodC,
          findMethod(methods, "c"));

      cat.info("SUCCESS");
    }
  }


  /**
   * Test for getAllAccessibleFields(from) method.
   */
  public void testGetAllAccessibleFields() throws Exception {
    cat.info("Testing getAllAccessibleFields(from)");

    Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("getAllAccessibleFields"));
    project.getProjectLoader().build();

    final BinCIType test =
        project.getTypeRefForName("Test").getBinCIType();

    final BinCIType A =
        project.getTypeRefForName("a.A").getBinCIType();

    final BinCIType B =
        project.getTypeRefForName("b.B").getBinCIType();

    final BinCIType C =
        project.getTypeRefForName("b.C").getBinCIType();

    final BinCIType X1 =
        project.getTypeRefForName("a.X1").getBinCIType();

    final BinCIType X2 =
        project.getTypeRefForName("a.X2").getBinCIType();

    List fields = X1.getAccessibleFields(test);
    assertEquals("Number of fields: X1 from Test", 1, fields.size());
    assertEquals("Field", ((BinField) fields.get(0)).getQualifiedName(),
        "a.X1.X1_field");

    fields = X2.getAccessibleFields(test);
    assertEquals("Number of fields: X2 from Test", 0, fields.size());

    fields = A.getAccessibleFields(A);
    assertEquals("Number of fields: A from A", 6, fields.size());
    assertEquals("Field", ((BinField) fields.get(0)).getQualifiedName(),
        "a.A.A_field1");
    assertEquals("Field", ((BinField) fields.get(1)).getQualifiedName(),
        "a.A.A_field2");
    assertEquals("Field", ((BinField) fields.get(2)).getQualifiedName(),
        "a.A.A_field3");
    assertEquals("Field", ((BinField) fields.get(3)).getQualifiedName(),
        "a.A.A_field4");
    assertEquals("Field", ((BinField) fields.get(4)).getQualifiedName(),
        "a.X1.X1_field");
    assertEquals("Field", ((BinField) fields.get(5)).getQualifiedName(),
        "a.X2.X2_field");

    fields = A.getAccessibleFields(test);
    assertEquals("Number of fields: A from Test", 3, fields.size());
    assertEquals("Field", ((BinField) fields.get(0)).getQualifiedName(),
        "a.A.A_field4");
    assertEquals("Field", ((BinField) fields.get(1)).getQualifiedName(),
        "a.X1.X1_field");
    assertEquals("Field", ((BinField) fields.get(2)).getQualifiedName(),
        "a.X2.X2_field");

    fields = B.getAccessibleFields(test);
    assertEquals("Number of fields: B from Test", 4, fields.size());
    assertEquals("Field", ((BinField) fields.get(0)).getQualifiedName(),
        "b.B.B_field4");
    assertEquals("Field", ((BinField) fields.get(1)).getQualifiedName(),
        "a.A.A_field4");
    assertEquals("Field", ((BinField) fields.get(2)).getQualifiedName(),
        "a.X1.X1_field");
    assertEquals("Field", ((BinField) fields.get(3)).getQualifiedName(),
        "a.X2.X2_field");

    fields = C.getAccessibleFields(test);
    assertEquals("Number of fields: C from Test", 5, fields.size());
    assertEquals("Field", ((BinField) fields.get(0)).getQualifiedName(),
        "b.C.C_field4");
    assertEquals("Field", ((BinField) fields.get(1)).getQualifiedName(),
        "b.B.B_field4");
    assertEquals("Field", ((BinField) fields.get(2)).getQualifiedName(),
        "a.A.A_field4");
    assertEquals("Field", ((BinField) fields.get(3)).getQualifiedName(),
        "a.X1.X1_field");
    assertEquals("Field", ((BinField) fields.get(4)).getQualifiedName(),
        "a.X2.X2_field");

    fields = C.getAccessibleFields(C);
    assertEquals("Number of fields: C from C", 11, fields.size());
    assertEquals("Field", ((BinField) fields.get(0)).getQualifiedName(),
        "b.C.C_field1");
    assertEquals("Field", ((BinField) fields.get(1)).getQualifiedName(),
        "b.C.C_field2");
    assertEquals("Field", ((BinField) fields.get(2)).getQualifiedName(),
        "b.C.C_field3");
    assertEquals("Field", ((BinField) fields.get(3)).getQualifiedName(),
        "b.C.C_field4");
    assertEquals("Field", ((BinField) fields.get(4)).getQualifiedName(),
        "b.B.B_field1");
    assertEquals("Field", ((BinField) fields.get(5)).getQualifiedName(),
        "b.B.B_field2");
    assertEquals("Field", ((BinField) fields.get(6)).getQualifiedName(),
        "b.B.B_field4");
    assertEquals("Field", ((BinField) fields.get(7)).getQualifiedName(),
        "a.A.A_field2");
    assertEquals("Field", ((BinField) fields.get(8)).getQualifiedName(),
        "a.A.A_field4");
    assertEquals("Field", ((BinField) fields.get(9)).getQualifiedName(),
        "a.X1.X1_field");
    assertEquals("Field", ((BinField) fields.get(10)).getQualifiedName(),
        "a.X2.X2_field");

    fields = C.getAccessibleFields(A);
    assertEquals("Number of fields: C from A", 6, fields.size());
    assertEquals("Field", ((BinField) fields.get(0)).getQualifiedName(),
        "b.C.C_field4");
    assertEquals("Field", ((BinField) fields.get(1)).getQualifiedName(),
        "b.B.B_field4");
    assertEquals("Field", ((BinField) fields.get(2)).getQualifiedName(),
        "a.A.A_field2");
    assertEquals("Field", ((BinField) fields.get(3)).getQualifiedName(),
        "a.A.A_field4");
    assertEquals("Field", ((BinField) fields.get(4)).getQualifiedName(),
        "a.X1.X1_field");
    assertEquals("Field", ((BinField) fields.get(5)).getQualifiedName(),
        "a.X2.X2_field");
  }

  /**
   * Tests that: types within SourcePath have getOffsetNode() != null,
   * types NOT in SourcePath have getOffsetNode == null
   * AST is of type CLASS_DEF or INTERFACE_DEF
   */
  public void testGetOffsetNode() throws Exception {
    cat.info("Testing BinCIType.getOffsetNode()");

    Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("getAllAccessibleFields"));
    project.getProjectLoader().build();

    final BinCIType A
        = project.getTypeRefForName("a.A").getBinCIType();

    assertTrue("a.A has an AST", A.getOffsetNode() != null);
    assertEquals("a.A has an AST of type CLASS_DEF",
        A.getOffsetNode().getType(), JavaTokenTypes.CLASS_DEF);

    final BinCIType X
        = project.getTypeRefForName("a.X1").getBinCIType();

    assertTrue("a.X1 has an AST", X.getOffsetNode() != null);
    assertEquals("a.X1 has an AST of type INTERFACE_DEF",
        X.getOffsetNode().getType(), JavaTokenTypes.INTERFACE_DEF);

    final BinCIType object
        = project.getObjectRef().getBinCIType();

    assertTrue("java.lang.Object doesn't have an AST",
        object.getOffsetNode() == null);
  }
}
