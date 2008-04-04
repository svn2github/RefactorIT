/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.modifiers.MinimizeAccessRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ApplyMinimalAccessActionTest extends CorrectiveActionTest{

  public ApplyMinimalAccessActionTest(String name){
    super(name);
  }
  
  public static Test suite(){
    return new TestSuite(ApplyMinimalAccessActionTest.class);
  }
  
  public String getTemplate(){
    return "Audit/corrective/MinimizeAccess/"+
    "ApplyMinimalAccess/<in_out>/";
  }
  
  /******************************* Simple Tests *******************************/
  
  protected void performSimpleTest() throws Exception{
    super.performSimpleTest(MinimizeAccessRule.class,
        "refactorit.audit.action.minimize_access.apply_minimal");
  }
  
  public void test() throws Exception{
    performSimpleTest();
  }
}
