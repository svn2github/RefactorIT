/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.StringEqualComparisionRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author  Oleg Tsernetsov
 */
public class ChangeEqualsOrderTest extends CorrectiveActionTest{
  
   public ChangeEqualsOrderTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(ChangeToEqualsActionTest.class);
  }
  
  public String getTemplate() {
    return "Audit/corrective/StringEqualsOrder/" +
        "ChangeEqualsOrder/<in_out>/<test_name>.java";
  }
  
  /************************ Simple Tests ********************************/

  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(StringEqualComparisionRule.class,
        "refactorit.audit.action.str_equals_order.correct");
  }

  public void testStringCompare() throws Exception {
    performSimpleTest();
  }
}
