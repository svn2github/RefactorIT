/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.PackageUsageInfo;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.Source;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Represents complex nodes, e.g. package declaration or import to simplify
 * refactorings on it.
 *
 * @author Anton Safonov
 */
public class CompoundASTImpl extends SimpleASTImpl {

  ASTImpl node;

  public CompoundASTImpl(final ASTImpl node) {
    super.initialize(node);

    if (Assert.enabled) {
      Assert.must(node != null, "Node shouldn't be null!");
    }

    setNextSibling(node.getNextSibling());
    setParent(node.getParent());

    findStartEnd(node);
    setType(node.getType());
    setText(null); // will get it lazily, when someone asks
    this.node = node;

//    (new rantlr.debug.misc.ASTFrame(getText(), node)).setVisible(true);
  }

  public String getText() {
    String text = super.getText();
    if (text == null) {
      if (this.node == null) {
        text = "";
      } else {
        Source src = this.node.getSource();
        if (src != null && getStartLine() >= 0) {
          text = src.getText(getStartLine(), getStartColumn(),
              getEndLine(), getEndColumn());
        } else {
          text = getSubTreeText(this.node);
        }
        this.node = null; // don't need it anymore, let it go
      }
      setText(text);
    }

    return text;
  }

  private String getSubTreeText(final ASTImpl node) {
    final StringBuffer buffer = new StringBuffer();
    getSubTreeText(node, buffer);
    return buffer.toString();
  }

  // FIXME: works now only for package name nodes, should be much more complex,
  // check rantlr more carefully for ready code
  private void getSubTreeText(ASTImpl node, StringBuffer buffer) {
    if (node == null) {
      return;
    }

    ASTImpl child = (ASTImpl) node.getFirstChild();

    if (child == null) {
      buffer.append(node.getText());
    } else {
      getSubTreeText(child, buffer);
      buffer.append(node.getText());
      getSubTreeText((ASTImpl) child.getNextSibling(), buffer);
    }
  }

//  private Set loopBreaker = new HashSet();
  private final void findStartEnd(final ASTImpl node) {
//System.err.println("findFirstLastNodes: " + node);
//    if (this.loopBreaker.contains(node)) {
//      return;
//    }
//    this.loopBreaker.add(node);

    if (node.getLine() > 0) {
      // FIXME: strange code - must be checked and fixes elsewhere
      if (node.getEndLine() <= 0) {
        node.setEndLine(node.getLine());
      }
      if (node.getEndColumn() <= 0 && node.getColumn() > 0) {
        node.setEndColumn((short) (node.getColumn() + node.getText().length()
            + 1));
      }
      ////////////////////////////////////////////////////////////

      if (node.getStartLine() < getStartLine()
          || (node.getStartLine() == getStartLine()
          && node.getStartColumn() < getStartColumn())
          || getStartLine() == 0) {
        if (node.getStartLine() != 0) {
          setStartLine(node.getStartLine());
          setStartColumn(node.getStartColumn());
//System.err.println("new first: " + node);
        }
      }

      if (node.getEndLine() > getEndLine()
          || (node.getEndLine() == getEndLine()
          && node.getEndColumn() > getEndColumn())
          || getEndLine() == 0) {
        if (node.getEndLine() != 0) {
          setEndLine(node.getEndLine());
          setEndColumn(node.getEndColumn());
//System.err.println("new last: " + node);
        }
      }
    }

    ASTImpl child = (ASTImpl) node.getFirstChild();
    while (child != null) {
      findStartEnd(child);
      child = (ASTImpl) child.getNextSibling();
    }

//    this.loopBreaker.remove(node);
  }

  public static final ASTImpl compoundQualifiedNameAST(final ASTImpl node) {
    ASTImpl newnode = node;
    while (newnode != null && newnode.getParent() != null
        && newnode.getParent().getType() == JavaTokenTypes.DOT) {
      newnode = newnode.getParent();
    }
    if (newnode == null) {
      return node;
    } else {
      return new CompoundASTImpl(newnode);
    }
  }

  public static final ASTImpl compoundImportAST(final ASTImpl node) {
    ASTImpl newnode = node;
    while (newnode != null && newnode.getParent() != null
        && (newnode.getParent().getType() == JavaTokenTypes.DOT
        || newnode.getParent().getType() == JavaTokenTypes.IMPORT)) {
      newnode = newnode.getParent();
    }
    if (newnode == null) {
      return node;
    } else {
      return new CompoundASTImpl(newnode);
    }
  }

  public static ASTImpl compoundTypeAST(ASTImpl node) {

    ASTImpl newnode = node;

    if (node.getType() == JavaTokenTypes.ARRAY_DECLARATOR) {
      // array
      newnode = (ASTImpl) node.getFirstChild();
      return new CompoundASTImpl(newnode);
    } else {
      return compoundQualifiedNameAST(node);
    }
  }

  /** Test driver for {@link CompoundASTImpl}. */

  public static class TestDriver extends TestCase {

    private Project project;
    private BinTypeRef type;

    private static final Category cat =
        Category.getInstance(TestDriver.class.getName());

    public TestDriver(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(TestDriver.class);
      suite.setName("CompoundASTImpl tests");
      return suite;
    }

    public void setUp() throws Exception {
      project = Utils.createTestRbProject("CompoundAST");
      project.getProjectLoader().build();
      type = project.getTypeRefForName("a.b.c.Test");
      assertNotNull("TypeRef is not null", type);
    }

    /**
     * Tests getText for complex nodes
     */
    public void testGetText() {
// Disabled - who wants the text from the virtual node?
//      cat.info("Test getText for complex nodes");
//
//      ASTImpl node = new CompoundASTImpl(
//          ((PackageUsageInfo) type.getBinCIType().getCompilationUnit()
//          .getPackageUsageInfos().get(0)).getNode());
//
//      assertEquals("Node text", "a.b.c", node.getText());
//      cat.info("SUCCESS");
    }

    /**
     * Tests coordinates for one line complex node
     */
    public void testGetOneLineNodeCoordinates() {
      cat.info("Tests getting coordinates for one line complex node");

      ASTImpl node = new CompoundASTImpl(
          ((PackageUsageInfo) type.getBinCIType()
          .getCompilationUnit().getPackageUsageInfos().get(0)).getNode());

      assertEquals("start line", 1, node.getStartLine());
      assertEquals("start column", 9, node.getStartColumn());
      assertEquals("end line", 1, node.getEndLine());
      assertEquals("end column", 14, node.getEndColumn());

      cat.info("SUCCESS");
    }

    /**
     * Tests getting coordinates for multi line complex node
     */
    public void testMultiLineNodeCoordinates() {
      cat.info("Tests getting coordinates for multi line complex node");

      ASTImpl node = new CompoundASTImpl((ASTImpl)
          type.getBinCIType()
          .getCompilationUnit().getSource().getFirstNode().getNextSibling().
          getFirstChild());
      //System.err.println("Node: " + node.getText());

      assertEquals("start line", 3, node.getStartLine());
      assertEquals("start column", 8, node.getStartColumn());
      assertEquals("end line", 4, node.getEndLine());
      assertEquals("end column", 6, node.getEndColumn());

      cat.info("SUCCESS");
    }

    /**
     * Tests coordinates for fully qualified type in extends clause.
     */
    public void testQualifiedExtend() {
      cat.info("Tests coordinates for fully qualified type in extends clause");

      // should get "java.lang.Object"
      ASTImpl node = ((BinTypeRef) type.getBinCIType()
          .getSpecificSuperTypeRefs().get(0)).getNode();
      node = compoundQualifiedNameAST(node);
      //System.err.println("Node: " + node.getText());

      assertEquals("start line", 6, node.getStartLine());
      assertEquals("start column", 36, node.getStartColumn());
      assertEquals("end line", 7, node.getEndLine());
      assertEquals("end column", 8, node.getEndColumn());

      cat.info("SUCCESS");
    }

    /**
     * Tests coordinates for method invocation expression.
     */
    public void testMethodInvocatioin() {
      if (!RefactorItConstants.runNotImplementedTests) {
        return;
      }

      cat.info("Tests coordinates for method invocation expression");

      BinExpression methodInvocation
          = ((BinExpressionStatement) type.getBinCIType()
          .getDeclaredMethods()[0].getBody().getStatements()[0])
          .getExpression();

      ASTImpl node = new CompoundASTImpl(methodInvocation.getRootAst());
      //System.err.println("Node: " + node.getText());

      assertEquals("start line", 12, node.getStartLine());
      assertEquals("start column", 5, node.getStartColumn());
      assertEquals("end line", 12, node.getEndLine());
      assertEquals("end column", 13, node.getEndColumn());

      cat.info("SUCCESS");
    }

    /**
     * Tests
     */
    /*    public void testNode() {
          cat.info("Tests ");

          ASTImpl node = (ASTImpl) type.getBinCIType().getOffsetNode();
          new CompoundASTImpl(node);

          node = (ASTImpl) type.getBinCIType()
              .getDeclaredField("i").getOffsetNode();
          new CompoundASTImpl(node);

          cat.info("SUCCESS");
        }*/
  }

}
