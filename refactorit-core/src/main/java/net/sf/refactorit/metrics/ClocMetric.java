/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.metrics;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Comment Lines of Code (CLOC) metric.
 *
 * <h3>Example</h3>
 * <code><pre>
 *void test() {
 *
 *  // Hi!
 *  System.out.println("Hi!");
 *
 *  // This one throws exception
 *  throw new RuntimeException(
 *      "Hi!");
 *}
 * </pre></code>
 *
 * <code>CLOC = 2</code>.
 */
public class ClocMetric {

  /** Hidden constructor. */
  private ClocMetric() {}

  /**
   * Calculates CLOC for a package.
   *
   * @param package
   * @return CLOC of the package
   **/
  public static int calculate(BinPackage pkg) {
    List sources = pkg.getCompilationUnitList();
    int sourceCount = sources.size();
    int lineCount = 0;
    for (int i = 0; i < sourceCount; i++) {
      lineCount += getFileCloc((CompilationUnit) sources.get(i));
    }
    return lineCount;
  }

  private static int getFileCloc(CompilationUnit compilationUnit) {
    int lineCount = 0;
    lineCount += getCommentsSize(compilationUnit.getJavadocComments());
    lineCount += getCommentsSize(compilationUnit.getSimpleComments());
    return lineCount;
  }

  private static int getCommentsSize(List comments) {
    int lineCount = 0;
    for (int i = 0; i < comments.size(); i++) {
      final Comment comment = (Comment) comments.get(i);
      lineCount += (comment.getEndLine() - comment.getStartLine() + 1);
    }
    return lineCount;
  }

  /**
   * Calculates CLOC for the member.
   *
   * @param member member.
   *
   * @return CLOC of the <code>member</code>.
   */
  public static int calculate(BinMember member) {
    if (member instanceof BinMethod) {
      return calculateForAst(((BinMethod) member).getBodyAST(),
          member.getCompilationUnit());
    }
    if (member instanceof BinCIType) {
      return calculateForAst(((BinCIType) member).getBodyAST(),
          member.getCompilationUnit());
    } else if (member instanceof BinInitializer) {
      return calculateForAst(((BinInitializer) member).getBodyAST(),
          member.getCompilationUnit());
    } else {
      throw new IllegalArgumentException(
          "Don't know how to calculate CLOC for "
          + " " + member.getClass());
    }
  }

  /**
   * Calculates CLOC for the AST node.
   *
   * @param node AST node.
   * @param compilationUnit file this node is located in.
   *
   * @return CLOC of the <code>node</code>.
   */
  private static int calculateForAst(ASTImpl node,
      CompilationUnit source) {
    if (node == null) {
      return 0;
    }

    final int nodeStartLine = node.getStartLine();
    final int nodeEndLine = node.getEndLine();
    final int nodeStartColumn = node.getStartColumn();
    final int nodeEndColumn = node.getEndColumn();

    int lastLineCounted = -1;
    int commentLines = 0;

    // Using .get(i) is faster on ArrayList than Iterator.next()
    List simple = source.getSimpleComments();
    List javadoc = source.getJavadocComments();
    final int simpleCount = simple.size();
    final int javadocCount = javadoc.size();
    int simpleIndex = 0;
    int javadocIndex = 0;
    while ((simpleIndex < simpleCount) || (javadocIndex < javadocCount)) {
      final Comment javadocComment =
          (javadocIndex < javadocCount)
          ? (Comment) javadoc.get(javadocIndex) : null;
      final Comment simpleComment =
          (simpleIndex < simpleCount)
          ? (Comment) simple.get(simpleIndex) : null;
      final Comment comment;
      if ((javadocComment != null) && (simpleComment != null)) {
        if (javadocComment.getStartLine() < simpleComment.getStartLine()) {
          comment = javadocComment;
          javadocIndex++;
        } else if (javadocComment.getStartLine()
            > simpleComment.getStartLine()) {
          comment = simpleComment;
          simpleIndex++;
        } else {
          // Both comments are on the same line
          if (javadocComment.getStartColumn()
              < simpleComment.getStartColumn()) {
            comment = javadocComment;
            javadocIndex++;
          } else {
            comment = simpleComment;
            simpleIndex++;
          }
        }
      } else {
        if (javadocComment == null) {
          comment = simpleComment;
          simpleIndex++;
        } else {
          comment = javadocComment;
          javadocIndex++;
        }
      }

      final int commentFirstLine = comment.getStartLine();
      // Check whether comments is inside this node
      // A rough check...
      if ((commentFirstLine < nodeStartLine)
          || (commentFirstLine > nodeEndLine)) {
        continue;
      }
      // More precise check
      if (((commentFirstLine == nodeStartLine)
          && (comment.getStartColumn() < nodeStartColumn))

          || ((commentFirstLine == nodeEndLine)
          && (comment.getStartColumn() >= nodeEndColumn))) {
        continue;
      }

      // This comment is inside the node
      final int commentStartLine = comment.getStartLine();
      final int commentEndLine = comment.getEndLine();
      if (commentStartLine == lastLineCounted) {
        // This comment start on the line last comments ends.
        commentLines += commentEndLine - commentStartLine;
      } else {
        commentLines += commentEndLine - commentStartLine + 1;
      }
      lastLineCounted = commentEndLine;
    }

    return commentLines;
  }

  /** Test driver for {@link ClocMetric}. */
  public static class TestDriver extends TestCase {
    /** Logger instance. */
    private static final Category cat =
        Category.getInstance(TestDriver.class.getName());

    /** Test project. */
    private Project project;

    public TestDriver(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(TestDriver.class);
      suite.setName("CLOC metric tests");
      return suite;
    }

    protected void setUp() throws Exception {
      project =
          Utils.createTestRbProject(Utils.getTestProjects().getProject("LOC"));
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests CLOC for class Test1.
     */
    public void testTest1() {
      cat.info("Testing CLOC for class Test1");
      assertEquals("CLOC", 0, getClocForType("Test1"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for class Test2.
     */
    public void testTest2() {
      cat.info("Testing CLOC for class Test2");
      assertEquals("CLOC", 0, getClocForType("Test2"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for class Test3.
     */
    public void testTest3() {
      cat.info("Testing CLOC for class Test3");
      assertEquals("CLOC", 0, getClocForType("Test3"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for class Test4.
     */
    public void testTest4() {
      cat.info("Testing CLOC for class Test4");
      assertEquals("CLOC", 0, getClocForType("Test4"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for class Test5.
     */
    public void testTest5() {
      cat.info("Testing CLOC for class Test5");
      assertEquals("CLOC", 1, getClocForType("Test5"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for class Test6.
     */
    public void testTest6() {
      cat.info("Testing CLOC for class Test6");
      assertEquals("CLOC", 1, getClocForType("Test6"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for class Test.
     */
    public void testTest() {
      cat.info("Testing CLOC for class Test");
      assertEquals("CLOC", 15, getClocForType("Test"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for Test constructor.
     */
    public void testTestConstructor() {
      cat.info("Testing CLOC for Test constructor");
      final BinClass test = (BinClass) getType("Test");
      assertEquals(
          "CLOC",
          0,
          ClocMetric.calculate(test.getDeclaredConstructors()[0]));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for Test second constructor.
     */
    public void testTestConstructor2() {
      cat.info("Testing CLOC for Test second constructor");
      final BinClass test = (BinClass) getType("Test");
      assertEquals(
          "CLOC",
          0,
          ClocMetric.calculate(test.getDeclaredConstructors()[1]));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for method Test.a.
     */
    public void testTestA() {
      cat.info("Testing CLOC for method Test.a");
      assertEquals("CLOC", 0, getClocForMethod("a"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for method Test.b.
     */
    public void testTestB() {
      cat.info("Testing CLOC for method Test.b");
      assertEquals("CLOC", 0, getClocForMethod("b"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for method Test.c.
     */
    public void testTestC() {
      cat.info("Testing CLOC for method Test.c");
      assertEquals("CLOC", 0, getClocForMethod("c"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for method Test.d.
     */
    public void testTestD() {
      cat.info("Testing CLOC for method Test.d");
      assertEquals("CLOC", 0, getClocForMethod("d"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for method Test.e.
     */
    public void testTestE() {
      cat.info("Testing CLOC for method Test.e");
      assertEquals("CLOC", 0, getClocForMethod("e"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for method Test.f.
     */
    public void testTestF() {
      cat.info("Testing CLOC for method Test.f");
      assertEquals("CLOC", 1, getClocForMethod("f"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for method Test.g.
     */
    public void testTestG() {
      cat.info("Testing CLOC for method Test.g");
      assertEquals("CLOC", 4, getClocForMethod("g"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for method Test.h.
     */
    public void testTestH() {
      cat.info("Testing CLOC for method Test.h");
      assertEquals("CLOC", 3, getClocForMethod("h"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for method Test.i.
     */
    public void testTestI() {
      cat.info("Testing CLOC for method Test.i");
      assertEquals("CLOC", 0, getClocForMethod("i"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CLOC for method Test.j.
     */
    public void testTestJ() {
      cat.info("Testing CLOC for method Test.j");
      assertEquals("CLOC", 3, getClocForMethod("j"));
      cat.info("SUCCESS");
    }

    /**
     * Gets type for FQN from test project.
     *
     * @param fqn type's FQN.
     *
     * @return type or <code>null</code> if type cannot be found.
     */
    private BinCIType getType(String fqn) {
      return (project.getTypeRefForName(fqn)).getBinCIType();
    }

    /**
     * Gets CLOC metric for type from test project.
     *
     * @param fqn type's FQN.
     *
     * @return CLOC metric.
     */
    private int getClocForType(String fqn) {
      final BinCIType type = getType(fqn);
      if (type == null) {
        throw new IllegalArgumentException("Type " + fqn + " not found");
      }

      return ClocMetric.calculate(type);
    }

    /**
     * Gets CLOC metric for method from Test class of test project.
     *
     * @param name name of the method.
     *
     * @return CLOC metric.
     */
    private int getClocForMethod(String name) {
      final BinCIType type = getType("Test");
      if (type == null) {
        throw new IllegalArgumentException("Type Test not found");
      }

      final BinMethod method
          = type.getDeclaredMethod(name, BinTypeRef.NO_TYPEREFS);

      return ClocMetric.calculate(method);
    }
  }
}
