/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.NotUsedRulesAddOn;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RemoveNotUsedMemberActionTest extends CorrectiveActionTest {
  
  public RemoveNotUsedMemberActionTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(RemoveNotUsedMemberActionTest.class);
  }
  
  public String getTemplate() {
    return "Audit/corrective/NotUsed/RemoveNotUsedMember/" +
        "<in_out>/";
  }
  
  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(NotUsedRulesAddOn.class,
        "refactorit.audit.action.not_used.remove_member");
  }
  
  public void testRemoveMembers() throws Exception {
    performSimpleTest();
  }
}
