/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;

import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.cli.StringArrayArguments;
import net.sf.refactorit.cli.actions.AuditModelBuilder;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.ui.treetable.writer.CommaSeparatedTableFormat;
import net.sf.refactorit.ui.treetable.writer.PlainTextTableFormat;
import net.sf.refactorit.ui.treetable.writer.TableFormat;

import junit.framework.TestCase;


/**
 * @author RISTO A
 */
public class ArgumentsTest extends TestCase {
  Arguments p;

  public ArgumentsTest(String name) {
    super(name);
  }

  public void testGetSourcepath() {
    p = new StringArrayArguments(new String[] {"-nogui", "-sourcepath", ".."});
    assertEquals("..", p.getSourcepath());
  }

  public void testGetClasspath() {
    p = new StringArrayArguments(new String[] {"-nogui", "-classpath", ".."});
    assertEquals("..", p.getClasspath());
  }

  public void testNonexistingParameter() {
    p = new StringArrayArguments(new String[0]);
    assertEquals("", p.getSourcepath());
  }

  public void testRequestedAction() {
    p = new StringArrayArguments(new String[] {"-notused"});
    assertTrue(p.isNotUsedAction());

    p = new StringArrayArguments(new String[] {"-metrics"});
    assertTrue(p.isMetricsAction());

    p = new StringArrayArguments(new String[] {"-audit"});
    assertTrue(p.isAuditAction());
  }

  public void testGetFormat() {
    p = new StringArrayArguments(new String[] {"-format", Arguments.HTML});
    assertEquals("html", p.getFormat());
  }

  public void testFormat() {
    p = new StringArrayArguments(new String[] {"-format", Arguments.HTML});
    assertTrue(p.isHtmlFormat());

    p = new StringArrayArguments(new String[] {"-format", Arguments.TEXT});
    assertTrue(p.isTextFormat());

    p = new StringArrayArguments(new String[] {"-format",
        Arguments.COMMA_SEPARATED});
    assertTrue(p.isCommaSeparatedFormat());

    p = new StringArrayArguments(new String[] {"-format", ""});
    assertTrue(p.isTextFormat());

    p = new StringArrayArguments(new String[0]);
    assertTrue(p.isTextFormat());
  }

  public void testWarnings_missingParameters() {
    p = new StringArrayArguments(new String[] {"-sourcepath", ".", "-classpath",
        "."});
    assertEquals(null, p.getWarning());

    p = new StringArrayArguments(new String[] {"-sourcepath", "."});
    assertEquals("WARNING: -classpath parameter is missing", p.getWarning());

    p = new StringArrayArguments(new String[] {"-classpath", "."});
    assertEquals("WARNING: -sourcepath parameter is missing", p.getWarning());

    p = new StringArrayArguments(new String[] {});
    assertEquals("WARNING: -sourcepath parameter is missing", p.getWarning());
  }

  public void testWarnings_emptyParameters() {
    p = new StringArrayArguments(new String[] {"-sourcepath", ".", "-classpath"});
    assertEquals("WARNING: -classpath parameter is empty", p.getWarning());

    p = new StringArrayArguments(new String[] {"-sourcepath", "-classpath", "."});
    assertEquals("WARNING: -sourcepath parameter is empty", p.getWarning());
  }

  /**
   * Why this? Under Windows, using the current run.bat file, the path items
   * are broken down into separate parameters by Windows. For example,
   * a parameter "x;y;z" will be passed to our app as 3 separate params, "x y z".
   */
  public void testSourcepathAndClasspathArePathParameters() {
    p = new StringArrayArguments(new String[] {
        "-sourcepath", "s1", "s2",
        "-classpath", "c1", "c2"});
    assertEquals("s1" + StringUtil.PATH_SEPARATOR + "s2",
        p.getSourcepath());
    assertEquals("c1" + StringUtil.PATH_SEPARATOR + "c2",
        p.getClasspath());
  }

  public void testGetFormatInstance() {
    TableFormat f = new StringArrayArguments("-format "
        + Arguments.TEXT).getFormatInstance();
    assertEquals(PlainTextTableFormat.class, f.getClass());

    f = new StringArrayArguments("-format "
        + Arguments.COMMA_SEPARATED).getFormatInstance();
    assertEquals(CommaSeparatedTableFormat.class, f.getClass());
  }

  public void testNonsupportedFormat() {
    TableFormat f = new StringArrayArguments("-format unknwon").
        getFormatInstance();
    assertNull(f);
  }

  public void testAuditAction() throws Exception {
    assertEquals(AuditModelBuilder.class,
        new StringArrayArguments("-audit").getModelBuilder().getClass());
  }

}
