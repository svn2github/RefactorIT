/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.classmodel;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test driver for {@link BinInitializer}.
 */
public abstract class BinInitializerTest {

  public static TestSuite suite() {
    final TestSuite suite = new TestSuite("BinInitializer tests");
    suite.addTest(GetBodyASTTest.suite());
    return suite;
  }

  /**
   * Test driver for {@link BinInitializer#getBodyAST}.
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
     * Tests getBodyAST on Test static initializer.
     */
    public void testGetBodyASTTestMain() throws Exception {
      cat.info("Testing getBodyAST on Test static initializer");

      final BinClass test =
          (BinClass)
          project.getTypeRefForName("Test").getBinCIType();
      // Test static initializer
      final ASTImpl bodyAst = test.getInitializers()[0].getBodyAST();
      assertTrue("getBodyAST() != null", bodyAst != null);
      assertEquals("Body start line", 4, bodyAst.getStartLine());
      assertEquals("Body start column", 10, bodyAst.getStartColumn());
      assertEquals("Body end line", 6, bodyAst.getEndLine());
      assertEquals("Body end column", 4, bodyAst.getEndColumn());

      cat.info("SUCCESS");
    }

    /**
     * Tests getBodyAST on abstract Test instance initializer.
     */
    public void testGetBodyASTTest3Main() throws Exception {
      cat.info("Testing getBodyAST abstract Test instance initializer");

      final BinClass test =
          (BinClass)
          project.getTypeRefForName("Test")
          .getBinCIType();

      // Test instance initializer
      final ASTImpl bodyAst = test.getInitializers()[1].getBodyAST();
      assertTrue("getBodyAST() != null", bodyAst != null);
      assertEquals("Body start line", 8, bodyAst.getStartLine());
      assertEquals("Body start column", 3, bodyAst.getStartColumn());
      assertEquals("Body end line", 10, bodyAst.getEndLine());
      assertEquals("Body end column", 4, bodyAst.getEndColumn());

      cat.info("SUCCESS");
    }
  }
}
