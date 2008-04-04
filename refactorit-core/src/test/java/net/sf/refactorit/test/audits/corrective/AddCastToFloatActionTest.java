/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.IntDivFloatContextRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author  Arseni Grigorjev
 */
public class AddCastToFloatActionTest extends CorrectiveActionTest{
  
  public AddCastToFloatActionTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(AddCastToFloatActionTest.class);
  }
  
  public String getTemplate() {
    return "Audit/corrective/IntDivFloatContext/AddCastToFloatAction/" +
        "<in_out>/<test_name>.java";
  }
  
  /************************ Simple Tests ********************************/

  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(IntDivFloatContextRule.class,
        "refactorit.audit.action.int_division.add_cast");
  }

  public void testInExpression() throws Exception {
    performSimpleTest();
  }
  
  public void testInMethod() throws Exception {
    performSimpleTest();
  }
  
  public void testInNew() throws Exception {
    performSimpleTest();
  }
  
}
