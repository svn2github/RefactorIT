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


public class CommentNotUsedMemberActionTest extends CorrectiveActionTest {
  
  public CommentNotUsedMemberActionTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(CommentNotUsedMemberActionTest.class);
  }
  
  public String getTemplate() {
    return "Audit/corrective/NotUsed/CommentNotUsedMember/" +
        "<in_out>/";
  }
  
  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(NotUsedRulesAddOn.class,
        "refactorit.audit.action.not_used.comment_member");
  }
  
  public void testCommentMembers() throws Exception {
    performSimpleTest();
  }
}
