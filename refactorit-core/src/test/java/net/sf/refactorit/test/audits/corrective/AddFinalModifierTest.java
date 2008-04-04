/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.modifiers.FinalMethodProposalRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author Arseni Grigorjev
 */
public class AddFinalModifierTest extends CorrectiveActionTest{
  
  public AddFinalModifierTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(AddFinalModifierTest.class);
  }
  
  public String getTemplate() {
    return "Audit/corrective/FinalMethodProposal/AddFinalModifier/" +
        "<in_out>/<test_name>.java";
  }
  
  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(FinalMethodProposalRule.class,
        "refactorit.audit.action.final.add_to_method");
  }
  
  public void testChangeModifiers() throws Exception {
    performSimpleTest();
  }
}
