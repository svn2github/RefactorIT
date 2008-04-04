/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.j2se5.RedundantBoxingRule;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author Arseni Grigorjev
 */
public class RemoveRedundantBoxingActionTest extends CorrectiveActionTest{

  private int oldJvmMode;

  public RemoveRedundantBoxingActionTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(RemoveRedundantBoxingActionTest.class);
  }

  public String getTemplate() {
    return "Audit/corrective/BoxingUnboxing/RemoveRedundantBoxingAction/" +
        "<in_out>/<test_name>.java";
  }

  protected void setUp() throws Exception {
    super.setUp();
    oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    Project.getDefaultOptions().setJvmMode(oldJvmMode);
  }

  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(RedundantBoxingRule.class,
        "refactorit.audit.action.remove_boxing.safe");
  }

  public void testBoxing_A() throws Exception {
    performSimpleTest();
  }
}
