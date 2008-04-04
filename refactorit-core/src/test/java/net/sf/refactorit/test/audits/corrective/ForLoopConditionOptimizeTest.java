/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.performance.ForLoopConditionOptimizer;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;


public class ForLoopConditionOptimizeTest extends CorrectiveActionTest {
  public ForLoopConditionOptimizeTest(final String name) {
    super(name);
  }

  public String getTemplate() {
    return "Audit/corrective/ForLoopConditionOptimizer/ForLoopPerformance" +
        "/<in_out>/<test_name>.java";
  }

  public static Test suite() {
    return new TestSuite(ForLoopConditionOptimizeTest.class);
  }

  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(ForLoopConditionOptimizer.class,
        "refactorit.audit.action.forloopcondition.optimize");
  }

  public void test01() throws Exception {
    performSimpleTest();
  }

  public void test02() throws Exception {
    performSimpleTest();
  }

  public void test03() throws Exception {
    performSimpleTest();
  }

  public void test04() throws Exception {
    performSimpleTest();
  }

}
