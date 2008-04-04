/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.EmptyBlocksAndBodiesRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author  Arseni Grigorjev
 */
public class InsertEmptyBlockCommentActionTest extends CorrectiveActionTest{
  
  public InsertEmptyBlockCommentActionTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(InsertEmptyBlockCommentActionTest.class);
  }
  
  public String getTemplate() {
    return "Audit/corrective/EmptyBlocksAndBodies/" +
        "InsertEmptyBlockCommentAction/<in_out>/<test_name>.java";
  }
  
  /************************ Simple Tests ********************************/

  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(EmptyBlocksAndBodiesRule.class,
        "refactorit.audit.action.empty_blocks.mark");
  }

  public void testAddComments() throws Exception {
    performSimpleTest();
  }
    
}
