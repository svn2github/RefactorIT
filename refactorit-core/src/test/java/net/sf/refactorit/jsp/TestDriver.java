/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jsp;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.jsp.JspServletSourceMap.JspPageArea;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.Source;

import org.apache.log4j.Category;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test driver for {@link JspServletSourceMap}.
 */
public class TestDriver extends TestCase {
  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(TestDriver.class.getName());

  /** Test project. */
  private Project project;

  /** Information about last compiled page. */
  private JspPageInfo lastPageInfo;
  /** Source code of servlet generated from last compiled page. */
  private String lastServletSource;

  public TestDriver(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(TestDriver.class);
    suite.setName("JspServletSourceMap tests");
    return suite;
  }

  /**
   * Tests import statements.
   */
  public void testImports() throws Exception {
    cat.info("Testing import statements");

    compileJsp("helloworld.jsp");
    assertMappingFound("java.util.Date");
    assertMappingFound("log4j.Cat");
    assertMappingFound("Date");
    assertMappingMissing("import");
    assertMappingMissing("Category;");
    assertMappingMissing("javax.servlet");

    cat.info("SUCCESS");
  }

  /**
   * Tests extends statement.
   */
  public void testExtends() throws Exception {
    cat.info("Testing extends statement");

    compileJsp("helloworld.jsp");
    assertMappingFound("AbstractHelloWorldServlet");
    assertMappingMissing("extends AbstractHelloWorldServlet");
    assertMappingMissing("AbstractHelloWorldServlet ");
    assertMappingMissing(" AbstractHelloWorldServlet");

    compileJsp("test.jsp");
    assertMappingMissing("HttpJspBase");

    cat.info("SUCCESS");
  }

  /**
   * Tests declaration.
   */
  public void testDeclaration() throws Exception {
    cat.info("Testing declaration");

    compileJsp("helloworld.jsp");
    assertMappingFound("Category cat");
    assertMappingFound("getInstance");
    assertMappingFound("boolean isValidCharacter");
    assertMappingFound("isLetter");

    cat.info("SUCCESS");
  }

  /**
   * Tests scriptlet (block of Java code).
   */
  public void testScriptlet() throws Exception {
    cat.info("Testing scriptlet");

    compileJsp("helloworld.jsp");
    assertMappingFound("intValue()");
    assertMappingFound("new Double(");
    assertMappingFound("i = 13", "helloworld.jsf");
    assertMappingMissing("HttpServletRequest");
    assertMappingMissing("IOException");
    assertMappingMissing("out.print");

    cat.info("SUCCESS");
  }

  /**
   * Tests expressions.
   */
  public void testExpressions() throws Exception {
    cat.info("Testing expressions");

    compileJsp("helloworld.jsp");
    assertMappingFound("System.", "helloworld.jsf");
    assertMappingFound("currentTimeMillis", "helloworld.jsf");
    assertMappingFound("new Date");
    assertMappingMissing("getDefaultFactory");
    assertMappingMissing("releasePageContext");
    assertMappingMissing("out.print");

    cat.info("SUCCESS");
  }

  /**
   * Tests jsp:useBean.
   */
  public void testUseBean() throws Exception {
    cat.info("Testing jsp:useBean");

    compileJsp("test.jsp");
    assertMappingFound("com.myco.Customer");
    assertMappingFound("com.myco.Company");
    assertMappingFound("Company");
    assertMappingFound("Customer");
    assertMappingFound("getName");

    // FIXME: seems this is not true, check who knows how it should be
    if (RefactorItConstants.runNotImplementedTests) {
      assertMappingMissing("customer");
    }

    cat.info("SUCCESS");
  }

  /**
   * Ensures that test project is loaded.
   */
  private void ensureProjectLoaded() {
    if (project != null) {
      return; // Already loaded
    }

    try {
      project = Utils.createTestRbProject("jsp/mapping");
      if (project == null) {
        fail("Failed to load jsp/mapping test project");
      }
    } catch (Exception e) {
      throw new ChainableRuntimeException(
          "Failed to load jsp/mapping test project",
          e);
    }
  }

  /**
   * Gets source from test project's source path.
   *
   * @param path path to source.
   *
   * @return source. Never returns <code>null</code>.
   */
  private Source getTestSource(String path) {
    ensureProjectLoaded();

    final Source root = project.getPaths().getSourcePath().getRootSources()[0];
    final Source source = root.getChild(path);
    assertNotNull("Couldn't find source for path = \"" + path
        + "\" in " + root, source);

    return source;
  }

  /**
   * Compiles JSP page. Results are stored in instance fields.
   *
   * @param path path to page.
   */
  private void compileJsp(String path) {
    final Source page = getTestSource(path);
    final JspCompiler compiler = new JspCompiler(project.getPaths().getSourcePath());
    final StringWriter servletSourceOut = new StringWriter();
    try {
      lastPageInfo = compiler.compile(page, servletSourceOut);
    } catch (Exception e) {
      throw new ChainableRuntimeException(
          "Failed to compile JSP " + path,
          e);
    }
    lastServletSource = servletSourceOut.toString();
  }

  /**
   * Finds first area of document containing specified text.
   *
   * @param document document to search for text in.
   * @param text text to search for.
   *
   * @return area or <code>null</code> if text not found.
   */
  private JspPageArea findText(String document, String text) {
    final int index = document.indexOf(text);
    if (index == -1) {
      return null; // Not found
    }
    final LineIndexer indexer = new LineIndexer(document, -1);
    final SourceCoordinate start = indexer.posToLineCol(index);
    final SourceCoordinate end =
        indexer.posToLineCol(index + text.length());
    assertTrue("start != null", (start != null));
    assertTrue("end != null", (end != null));

    final JspPageArea area = new JspPageArea();
    area.startLine = start.getLine() - 1;
    area.startColumn = start.getColumn() - 1;
    area.endLine = end.getLine() - 1;
    area.endColumn = end.getColumn() - 1;
    return area;
  }

  /**
   * Asserts that text found in generated servlet maps properly to the JSP
   * page from which servlet was generated.
   *
   * @param text text.
   */
  private void assertMappingFound(String text) {
    assertMappingFound(text, lastPageInfo.getPage().getRelativePath());
  }

  /**
   * Asserts that text found in generated servlet maps properly to a JSP
   * page.
   *
   * @param text text.
   * @param jspPagePath path of JSP page from which text was taken when
   *        generating servlet source code.
   */
  private void assertMappingFound(String text, String jspPagePath) {
    final Source page = getTestSource(jspPagePath);
    if ((!page.equals(lastPageInfo.getPage()))
        && (!lastPageInfo.getIncludedPages().contains(page))) {
      fail("JSP page " + jspPagePath
          + " was not used during last compilation");
    }

    final String expectedPageContent;
    try {
      expectedPageContent = new String(page.getContent());
    } catch (IOException e) {
      throw new ChainableRuntimeException(
          "Failed to convert page content to string",
          e);
    }
    final JspPageArea expectedPageArea = findText(expectedPageContent, text);
    if (expectedPageArea == null) {
      throw new IllegalArgumentException("Text \"" + text
          + "\" not found in " + page);
    }
    expectedPageArea.page = page;

    final JspPageArea servletSourceArea = findText(lastServletSource, text);
    if (servletSourceArea == null) {
      throw new IllegalArgumentException("Text \"" + text
          + "\" not found in last generated servlet");
    }

    final JspPageArea actualPageArea =
        lastPageInfo.getServletSourceMap().mapArea(
        servletSourceArea.startLine,
        servletSourceArea.startColumn,
        servletSourceArea.endLine,
        servletSourceArea.endColumn);
    /*
           System.out.println("Text = " + text
      + ", servlet (" + servletSourceArea + ")"
      + ", expected = " + expectedPageArea
      + ", actual = " + actualPageArea);
     */
    assertEquals("JSP page area for text \"" + text
        + "\" at " + servletSourceArea,
        expectedPageArea,
        actualPageArea);
  }

  /**
   * Asserts that text found in generated servlet maps properly to a JSP
   * page.
   *
   * @param text text.
   * @param jspPagePath path of JSP page from which text was taken when
   *        generating servlet source code.
   */
  private void assertMappingMissing(String text) {
    final JspPageArea servletSourceArea = findText(lastServletSource, text);
    if (servletSourceArea == null) {
      throw new IllegalArgumentException("Text \"" + text
          + "\" not found in last generated servlet");
    }

    final JspPageArea actualPageArea =
        lastPageInfo.getServletSourceMap().mapArea(
        servletSourceArea.startLine,
        servletSourceArea.startColumn,
        servletSourceArea.endLine,
        servletSourceArea.endColumn);
    assertEquals("JSP page area for text \"" + text
        + "\" at " + servletSourceArea,
        null,
        actualPageArea);
  }
}
