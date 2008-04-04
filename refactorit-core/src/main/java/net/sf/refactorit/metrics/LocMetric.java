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
 * Lines of Code (LOC) metric.
 */
public class LocMetric {

  /** Hidden constructor. */
  private LocMetric() {}

  /**
   * Calculates LOC for the member.
   *
   * @param member member.
   *
   * @return LOC of the <code>member</code>.
   */
  public static int calculate(BinMember member) {
    //int emptyLines = getEmptyLinesCount(member);
    if (member instanceof BinMethod) {
      
      return calculateForAst(((BinMethod) member).getBodyAST(),
          member.getCompilationUnit());
    } else if (member instanceof BinCIType) {
      return calculateForAst(((BinCIType) member).getBodyAST(),
          member.getCompilationUnit());
    } else if (member instanceof BinInitializer) {
      return calculateForAst(((BinInitializer) member).getBodyAST(),
          member.getCompilationUnit());
    } else {
      throw new IllegalArgumentException("Don't know how to calculate LOC for "
          + " " + member.getClass());
    }
  }

  public static int calculate(BinPackage pkg) {
    List sources = pkg.getCompilationUnitList();
    int sourceCount = sources.size();
    int lineCount = 0;
    for (int i = 0; i < sourceCount; i++) {
      lineCount += getFileLoc((CompilationUnit) sources.get(i));
    }
    return lineCount;
  }

  private static int getFileLoc(CompilationUnit compilationUnit) {
    int lineCount = 0;
    String content = compilationUnit.getContent();
    for (int j = 0; j < content.length(); j++) {
      char c = content.charAt(j);
      if (c == '\n') {
        ++lineCount;
      }
    }
    return lineCount;
  }

  private static int getEmptyLinesCount(BinMember member){
    int lineCount = 0;
    
    String content = member.getText();
    boolean isLineEmpty = true;
    char c = ' ';
    for (int j = 0; j < content.length(); j++) {
      
      if(!(c=='\r' || c == '\t' || c =='\n' || c==' ')){
        isLineEmpty=false;
      }
      
      c = content.charAt(j);
      if (c == '\n') {
        if(isLineEmpty){
          ++lineCount;
        } else {
          isLineEmpty = true;
        }
      }
    }
    return lineCount;
  }
  /**
   * Calculates LOC for the AST node.
   *
   * @param node AST node.
   * @param compilationUnit file this node is located in.
   *
   * @return LOC of the <code>node</code>.
   */
  private static int calculateForAst(ASTImpl node, CompilationUnit source) {
    
    if (node == null) {
      return 0;
    }
    final int nodeStartLine = node.getStartLine();
    final int nodeEndLine = node.getEndLine();

    /**
     * Physical SLOC
     */
    
    // number of lines between opening and closing braces
    int lines = nodeEndLine - nodeStartLine;
    
    final ASTImpl firstChild = (ASTImpl) node.getFirstChild();
    if (firstChild != null) {
      if(firstChild.getStartLine() == nodeStartLine){
        ++lines;
      }
      
      ASTImpl lastChild = firstChild;
      while(lastChild.getNextSibling()!= null){
        lastChild =(ASTImpl)lastChild.getNextSibling();
      }
      
      if(nodeEndLine != nodeStartLine && nodeEndLine == lastChild.getEndLine()){
        ++lines;
      }
    }
    
    if(nodeStartLine != nodeEndLine){
      --lines;
    }
    

    if(lines<0){
      return 0;
    }
    return lines;
    
    /**
     * a little bit unstable Logical SLOC
     */
    
    //final int nodeStartColumn = node.getStartColumn();
    //final int nodeEndColumn = node.getEndColumn();
    
    
    /*
    // These boundaries are adjested properly when code and comments are scanned
    int firstLine = nodeEndLine + 1;
    int lastLine = nodeStartLine - 1;

    final ASTImpl firstChild = (ASTImpl) node.getFirstChild();
    if (firstChild == null) {
      // No code, but comments might still be present
      // Comments are checked later, after this "if".
    } else {
      // At least one code AST
      firstLine = firstChild.getLine();
      ASTImpl lastChild = firstChild;
      ASTImpl nextChild = (ASTImpl) firstChild.getNextSibling();
      while (nextChild != null) {
        lastChild = nextChild;
        nextChild = (ASTImpl) nextChild.getNextSibling();
      }
      lastLine = lastChild.getEndLine();
    }

    if ((firstLine <= nodeStartLine) && (lastLine >= nodeEndLine)) {
      // No need to search further as LOC won't grow
      return lastLine - firstLine + 1;
    }

    // STANDARD COMMENTS
    // Using .get(i) is faster on ArrayList than Iterator.next()
    List comments = source.getSimpleComments();
    int commentCount = comments.size();
    for (int i = 0; i < commentCount; i++) {
      final Comment comment = (Comment) comments.get(i);
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
      if (commentFirstLine < firstLine) {
        firstLine = commentFirstLine;
      }
      final int commentLastLine = comment.getEndLine();
      if (commentLastLine > lastLine) {
        lastLine = commentLastLine;
      }
    }

    if ((firstLine <= nodeStartLine) && (lastLine >= nodeEndLine)) {
      // No need to search further as LOC won't grow
      return lastLine - firstLine + 1;
    }

    // JAVADOC COMMENTS
    // Using .get(i) is faster on ArrayList than Iterator.next()
    comments = source.getJavadocComments();
    commentCount = comments.size();
    for (int i = 0; i < commentCount; i++) {
      final Comment comment = (Comment) comments.get(i);
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
      if (commentFirstLine < firstLine) {
        firstLine = commentFirstLine;
      }
      final int commentLastLine = comment.getEndLine();
      if (commentLastLine > lastLine) {
        lastLine = commentLastLine;
      }
    }

    if (lastLine < firstLine) {
      // Neither code nor comments found
      return 0;
    } else {
      return lastLine - firstLine + 1;
    }*/
  }

  /** Test driver for {@link LocMetric}. */
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
      suite.setName("LOC metric tests");
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
     * Tests LOC for class Test1.
     */
    public void testTest1() {
      cat.info("Testing LOC for class Test1");
      assertEquals("LOC", 1, getLocForType("Test1"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for class Test2.
     */
    public void testTest2() {
      cat.info("Testing LOC for class Test2");
      assertEquals("LOC", 0, getLocForType("Test2"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for class Test3.
     */
    public void testTest3() {
      cat.info("Testing LOC for class Test3");
      assertEquals("LOC", 2, getLocForType("Test3"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for class Test4.
     */
    public void testTest4() {
      cat.info("Testing LOC for class Test4");
      assertEquals("LOC", 2, getLocForType("Test4"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for class Test5.
     */
    public void testTest5() {
      cat.info("Testing LOC for class Test5");
      assertEquals("LOC", 3, getLocForType("Test5"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for class Test6.
     */
    public void testTest6() {
      cat.info("Testing LOC for class Test6");
      assertEquals("LOC", 5, getLocForType("Test6"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for class Test.
     */
    public void testTest() {
      cat.info("Testing LOC for class Test");
      assertEquals("LOC", 76, getLocForType("Test"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for Test constructor.
     */
    public void testTestConstructor() {
      cat.info("Testing LOC for Test constructor");
      final BinClass test = (BinClass) getType("Test");
      assertEquals(
          "LOC",
          1,
          LocMetric.calculate(test.getDeclaredConstructors()[0]));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for Test second constructor.
     */
    public void testTestConstructor2() {
      cat.info("Testing LOC for Test second constructor");
      final BinClass test = (BinClass) getType("Test");
      assertEquals(
          "LOC",
          1,
          LocMetric.calculate(test.getDeclaredConstructors()[1]));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for method Test.a.
     */
    public void testTestA() {
      cat.info("Testing LOC for method Test.a");
      assertEquals("LOC", 1, getLocForMethod("a"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for method Test.b.
     */
    public void testTestB() {
      cat.info("Testing LOC for method Test.b");
      assertEquals("LOC", 0, getLocForMethod("b"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for method Test.c.
     */
    public void testTestC() {
      cat.info("Testing LOC for method Test.c");
      assertEquals("LOC", 1, getLocForMethod("c"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for method Test.d.
     */
    public void testTestD() {
      cat.info("Testing LOC for method Test.d");
      assertEquals("LOC", 1, getLocForMethod("d"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for method Test.e.
     */
    public void testTestE() {
      cat.info("Testing LOC for method Test.e");
      assertEquals("LOC", 6, getLocForMethod("e"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for method Test.f.
     */
    public void testTestF() {
      cat.info("Testing LOC for method Test.f");
      assertEquals("LOC", 2, getLocForMethod("f"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for method Test.g.
     */
    public void testTestG() {
      cat.info("Testing LOC for method Test.g");
      assertEquals("LOC", 8, getLocForMethod("g"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for method Test.h.
     */
    public void testTestH() {
      cat.info("Testing LOC for method Test.h");
      assertEquals("LOC", 6, getLocForMethod("h"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for method Test.i.
     */
    public void testTestI() {
      cat.info("Testing LOC for method Test.i");
      assertEquals("LOC", 0, getLocForMethod("i"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LOC for method Test.j.
     */
    public void testTestJ() {
      cat.info("Testing LOC for method Test.j");
      assertEquals("LOC", 10, getLocForMethod("j"));
      cat.info("SUCCESS");
    }

    public void testPackage() {
      cat.info("Testing LOC for package");
      assertEquals("LOC", 128, getLocForPackage(""));
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
     * Gets LOC metric for type from test project.
     *
     * @param fqn type's FQN.
     *
     * @return LOC metric.
     */
    private int getLocForType(String fqn) {
      final BinCIType type = getType(fqn);
      if (type == null) {
        throw new IllegalArgumentException("Type " + fqn + " not found");
      }

      return LocMetric.calculate(type);
    }

    /**
     * Gets LOC metric for method from Test class of test project.
     *
     * @param name name of the method.
     *
     * @return LOC metric.
     */
    private int getLocForMethod(String name) {
      final BinCIType type = getType("Test");
      if (type == null) {
        throw new IllegalArgumentException("Type Test not found");
      }

      final BinMethod method
          = type.getDeclaredMethod(name, BinTypeRef.NO_TYPEREFS);

      return LocMetric.calculate(method);
    }

    private int getLocForPackage(String name) {
      final BinPackage pkg = project.getPackageForName(name);
      return LocMetric.calculate(pkg);
    }
  }
}
