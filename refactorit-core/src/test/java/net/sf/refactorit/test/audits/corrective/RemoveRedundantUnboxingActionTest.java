/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.j2se5.RedundantUnboxingRule;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author Arseni Grigorjev
 */
public class RemoveRedundantUnboxingActionTest extends CorrectiveActionTest{

  private int oldJvmMode;

  public RemoveRedundantUnboxingActionTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(RemoveRedundantUnboxingActionTest.class);
  }

  public String getTemplate() {
    return "Audit/corrective/BoxingUnboxing/RemoveRedundantUnboxingAction/" +
        "<in_out>/";
  }

  protected void setUp() throws Exception {
    super.setUp();
    oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
  }

  protected void tearDown() throws Exception {
    Project.getDefaultOptions().setJvmMode(oldJvmMode);
    super.tearDown();
  }

  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(RedundantUnboxingRule.class,
        "refactorit.audit.action.remove_unboxing.safe");
  }

  public void testUnboxing_A() throws Exception {
    performSimpleTest();
  }

  public void testUnboxing_B() throws Exception {
    performSimpleTest();
  }

  public void testUnboxing_C() throws Exception {
    performSimpleTest();
  }
}
