/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.NestedBlockRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author Arseni Grigorjev
 */
public class RemoveBracketsActionTest extends CorrectiveActionTest{
  
  public RemoveBracketsActionTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(RemoveBracketsActionTest.class);
  }
  
  public String getTemplate() {
    return "Audit/corrective/NestedBlock/RemoveBracketsAction/" +
        "<in_out>/<test_name>.java";
  }
  
  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(NestedBlockRule.class,
        "refactorit.audit.action.remove_brackets");
  }
  
  public void testA() throws Exception {
    performSimpleTest();
  }
}

