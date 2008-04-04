/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.introducetemp;

import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.source.format.FormatSettings;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Anton Safonov
 */
public class CustomTest extends AllTests {
  public CustomTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(CustomTest.class);
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_TAB_SIZE, Integer.toString(2));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS, "true");
  }

  public String getTemplate() {
    return "IntroduceTemp/<stripped_test_name>/<in_out>/Test.java";
  }

  public void testbug2148() throws Exception {
    runTest(7, 5, 7, 30, true, true, "object", "object");
  }

  public void testIssue236() throws Exception {
    runTest(3, 12, 3, 32, true, true, "object", "object");
  }

  public void testIssue421() throws Exception {
    runTest(6, 31, 9, 6, true, true, "temp", "temp");
  }

  public void testRetTypeCondExpr1() throws Exception {
    runTest(4, 5, 4, 31, false, false, "obj", "obj");
  }

  public void testRetTypeCondExpr2() throws Exception {
    runTest(5, 5, 5, 31, false, false, "obj", "obj");
  }

  public void testRetTypeCondExpr3() throws Exception {
//    runTest(0, 0, 0, 0, false, false, "obj", "obj");
  }

  public void testRetTypeCondExpr4() throws Exception {
//    runTest(0, 0, 0, 0, false, false, "obj", "obj");
  }

  public void testRetTypeCondExpr5() throws Exception {
//    runTest(0, 0, 0, 0, false, false, "obj", "obj");
  }

  public static void m(boolean bool) {
    int b = 0;
    int i = b--;
	int c = bool ? b++ : i;
    System.out.println(c);
  }
}
