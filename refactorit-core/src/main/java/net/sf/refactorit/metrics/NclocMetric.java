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
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Non-comment Lines of Code (NCLOC, NCSL, ELOC) metric.
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
 * <code>NCLOC</code> is 3.
 */
public class NclocMetric {
  private int[] countedLines = new int[512];

  /** Hidden constructor. */
  private NclocMetric() {
  }

  private int getNcloc() {
    int count = 0;
    for (int i = 0; i < countedLines.length; i++) {
      if (countedLines[i] > 0) {
        ++count;
      }
    }
    return count;
  }

  public static int calculate(BinPackage pkg) {
    List sources = pkg.getCompilationUnitList();
    int sourceCount = sources.size();
    int lineCount = 0;
    for (int i = 0; i < sourceCount; i++) {
      lineCount += getFileNcloc((CompilationUnit) sources.get(i));
    }
    return lineCount;
  }

  private static int getFileNcloc(CompilationUnit compilationUnit) {
    ASTImpl ast = compilationUnit.getSource().getFirstNode();
    NclocMetric metric = new NclocMetric();
    metric.countAllSiblingLines(ast);
    return metric.getNcloc();
  }

  /**
   * Calculates NCLOC for the member.
   *
   * @param member member.
   *
   * @return NCLOC of the <code>member</code>.
   */
  public static int calculate(BinMember member) {
    NclocMetric metric = new NclocMetric();
    if (member instanceof BinCIType) {
      metric.calculateForAst(((BinCIType) member).getBodyAST(),
          member.getCompilationUnit());
      return metric.getNcloc();
    } else if (member instanceof BinMethod) {
      metric.calculateForAst(((BinMethod) member).getBodyAST(),
          member.getCompilationUnit());
      return metric.getNcloc();
    } else if (member instanceof BinInitializer) {
      metric.calculateForAst(((BinInitializer) member).getBodyAST(),
          member.getCompilationUnit());
      return metric.getNcloc();
    } else {
      throw new IllegalArgumentException("Don't know how to calculate NCLOC"
          + " for " + member.getClass());
    }
  }

  /**
   * Calculates NCLOC for the AST node.
   *
   * @param node AST node.
   * @param compilationUnit file this node is located in.
   *
   * @return NCLOC of the <code>node</code>.
   */
  private void calculateForAst(ASTImpl node, CompilationUnit source) {
    if (node == null) {
      return;
    }
    final ASTImpl firstChild = (ASTImpl) node.getFirstChild();
    if (firstChild == null) {
      // No code, but comments might still be present
      return;
    } else {
      countAllSiblingLines(firstChild);
    }
  }

  /*
     private static int countAllSiblingLines(ASTImpl firstChild) {
    // At least one code AST
    ASTImpl lastChild = firstChild;
    int lines = firstChild.getEndLine() - firstChild.getStartLine() + 1;
    int lastCountedLine = firstChild.getEndLine();
    ASTImpl nextChild = (ASTImpl) firstChild.getNextSibling();
    while (nextChild != null) {
      final int childStartLine = nextChild.getStartLine();
      final int childEndLine = nextChild.getEndLine();
      if (lastCountedLine < childStartLine) {
        // This child does not overlap with last child
        lines += childEndLine - childStartLine + 1;
      } else {
        // This child starts on the same line where last child ends
        lines += childEndLine - childStartLine;
      }
      lastCountedLine = childEndLine;

      lastChild = nextChild;
      nextChild = (ASTImpl) nextChild.getNextSibling();
    }
    return lines;
     }
   */

  private void countAllSiblingLines(ASTImpl node) {
    ASTImpl child = node;
    while (child != null) {
      int childStartLine = child.getStartLine();
      if (childStartLine > 0) {
        addLine(childStartLine);
      }
      countAllSiblingLines((ASTImpl) child.getFirstChild());
      int childEndLine = child.getEndLine();
      if (childEndLine > 0) {
        addLine(childEndLine);
      }
      child = (ASTImpl) child.getNextSibling();
    }
    return;
  }

  private void addLine(int line) {
    while (line >= countedLines.length) {
      int[] newLines = new int[line << 1];
      System.arraycopy(countedLines, 0, newLines, 0, countedLines.length);
      countedLines = newLines;
    }

    countedLines[line]++;
  }

  /** Test driver for {@link NclocMetric}. */
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
      suite.setName("NCLOC metric tests");
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
     * Tests NCLOC for Test constructor.
     */
    public void testTestConstructor() {
      cat.info("Testing NCLOC for Test constructor");
      final BinClass test = (BinClass) getType("Test");
      assertEquals(
          "NCLOC",
          1,
          NclocMetric.calculate(test.getDeclaredConstructors()[0]));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for Test second constructor.
     */
    public void testTestConstructor2() {
      cat.info("Testing NCLOC for Test second constructor");
      final BinClass test = (BinClass) getType("Test");
      assertEquals(
          "LOC",
          1,
          NclocMetric.calculate(test.getDeclaredConstructors()[1]));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for method Test.a.
     */
    public void testTestA() {
      cat.info("Testing NCLOC for method Test.a");
      assertEquals("NCLOC", 1, getLocForMethod("a"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for method Test.b.
     */
    public void testTestB() {
      cat.info("Testing NCLOC for method Test.b");
      assertEquals("NCLOC", 0, getLocForMethod("b"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for method Test.c.
     */
    public void testTestC() {
      cat.info("Testing NCLOC for method Test.c");
      assertEquals("NCLOC", 0, getLocForMethod("c"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for method Test.d.
     */
    public void testTestD() {
      cat.info("Testing NCLOC for method Test.d");
      assertEquals("NCLOC", 1, getLocForMethod("d"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for method Test.e.
     */
    public void testTestE() {
      cat.info("Testing NCLOC for method Test.e");
      assertEquals("NCLOC", 2, getLocForMethod("e"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for method Test.f.
     */
    public void testTestF() {
      cat.info("Testing NCLOC for method Test.f");
      assertEquals("NCLOC", 1, getLocForMethod("f"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for method Test.g.
     */
    public void testTestG() {
      cat.info("Testing NCLOC for method Test.g");
      assertEquals("NCLOC", 4, getLocForMethod("g"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for method Test.h.
     */
    public void testTestH() {
      cat.info("Testing NCLOC for method Test.h");
      assertEquals("NCLOC", 2, getLocForMethod("h"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for method Test.i.
     */
    public void testTestI() {
      cat.info("Testing NCLOC for method Test.i");
      assertEquals("NCLOC", 0, getLocForMethod("i"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NCLOC for method Test.j.
     */
    public void testTestJ() {
      cat.info("Testing NCLOC for method Test.j");
      assertEquals("NCLOC", 5, getLocForMethod("j"));
      cat.info("SUCCESS");
    }

    public void testTestBug1303() {
      cat.info("Testing NCLOC for method OtherTest.testBug1303");
      assertEquals("NCLOC", 3, getLocForMethod("OtherTest", "testBug1303"));
      cat.info("SUCCESS");
    }

    public void testPackage() {
      cat.info("Testing LOC for package");
      assertEquals("NCLOC", 79, getLocForPackage(""));
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

//    /**
//     * Gets LOC metric for type from test project.
//     *
//     * @param fqn type's FQN.
//     *
//     * @return LOC metric.
//     */
//    private int getLocForType(String fqn) {
//      final BinCIType type = getType(fqn);
//      if (type == null) {
//        throw new IllegalArgumentException("Type " + fqn + " not found");
//      }
//
//      return NclocMetric.calculate(type);
//    }

    /**
     * Gets LOC metric for method from Test class of test project.
     *
     * @param name name of the method.
     *
     * @return LOC metric.
     */
    private int getLocForMethod(String name) {
      return getLocForMethod("Test", name);
    }

    private int getLocForMethod(String className, String methodName) {
      final BinCIType type = getType(className);
      if (type == null) {
        throw new IllegalArgumentException("Type Test not found");
      }

      final BinMethod method
          = type.getDeclaredMethod(methodName, BinTypeRef.NO_TYPEREFS);

      return NclocMetric.calculate(method);
    }

    private int getLocForPackage(String name) {
      final BinPackage pkg = project.getPackageForName(name);
      return NclocMetric.calculate(pkg);
    }
  }
}
