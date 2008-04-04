/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.misc.DuplicateStringsRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class DuplicateStringsTest extends CorrectiveActionTest {

  public DuplicateStringsTest (String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(DuplicateStringsTest.class);
  }

  public String getTemplate() {
    return "Audit/corrective/DuplicateStrings/" +
        "DuplicateStringsCorrectiveAction/<in_out>/<test_name>.java";
  }

  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(DuplicateStringsRule.class,
        "refactorit.audit.action.duplicate_strings.substitute_with_constant");
  }

  public void test1() throws Exception {
    performSimpleTest();
  }
}
