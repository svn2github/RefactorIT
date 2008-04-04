/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.misc.EarlyDeclarationRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class EarlyDeclarationTest extends CorrectiveActionTest {

  public EarlyDeclarationTest (String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(EarlyDeclarationTest.class);
  }

  public String getTemplate() {
    return "Audit/corrective/EarlyDeclaration/" +
        "EarlyDeclarationCorrectiveAction/<in_out>/<test_name>.java";
  }

  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(EarlyDeclarationRule.class,
        "refactorit.audit.action.early_declaration.move_closer_to_first_use");
  }

  public void test2() throws Exception {
    performSimpleTest();
  }
}
