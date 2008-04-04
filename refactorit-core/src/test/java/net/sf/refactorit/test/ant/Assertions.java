/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.ant;




import net.sf.refactorit.ant.RefactorIt;
import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.test.RwRefactoringTestUtils;

import org.apache.tools.ant.BuildException;

import java.io.File;


public class Assertions extends RefactorIt {
  private String expectedFirst;
  private String expectedSecond;

  private String expected;
  private String got;

  private String testee;

  public void setExpectedFirst(String s) {
    expectedFirst = s;
  }

  public void setExpectedSecond(String s) {
    expectedSecond = s;
  }

  public void setExpected(String s) {
    expected = s;
  }

  public void setGot(String s) {
    got = s;
  }

  public void setTestee(String s) {
    testee = s;
  }

  public void execute() throws BuildException {
    if ("sourcepath".equals(testee)) {
      assertEqualsPath(expectedFirst + StringUtil.PATH_SEPARATOR
          + expectedSecond,
          getArguments().getSourcepath());
    } else if ("classpath".equals(testee)) {
      assertEqualsPath(expectedFirst + StringUtil.PATH_SEPARATOR
          + expectedSecond,
          getArguments().getClasspath());
    } else if ("all-options".equals(testee)) {
      Arguments a = getArguments();
      assertEquals("html", a.getFormat());
      assertEquals("profile", a.getProfile());
      assertEquals("output", a.getOutputFile());
      assertTrue(a.isMetricsAction());
    } else if ("action-parsing".equals(testee)) {
      setAction("metrics");
      assertTrue(getArguments().isMetricsAction());
      setAction("notused");
      assertTrue(getArguments().isNotUsedAction());
    } else if ("assert-same-file-contents".equals(testee)) {
      RwRefactoringTestUtils.compareWithDiff("", new File(expected),
          new File(got));
    } else if ("error-on-exec".equals(testee)) {
      try {
        RefactorIt task = new RefactorIt();
        task.execute();
      } catch (BuildException e) {
        // Good!
        assertEquals("ERROR: Action name missing", e.getMessage());
        return;
      }

      throw new BuildException(
          "Expected a build exception for missing action name");
    } else {
      throw new BuildException("Unknown testee: " + testee);
    }
  }

  // Assert methods

  public static void assertTrue(boolean b) throws BuildException {
    assertEquals(Boolean.TRUE, new Boolean(b));
  }

  public static void assertEquals(Object e, Object g) throws BuildException {
    if (!e.equals(g)) {
      throw new BuildException("Expected: " + e + ", got: " + g);
    }
  }

  public static void assertEqualsPath(String e, String g) {
    assertEquals(normalizePath(e), normalizePath(g));
  }

  private static String normalizePath(String p) {
    p = StringUtil.replace(p, "\\", "/");
    return p;
  }
}
