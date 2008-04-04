/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.exceptions.RedundantThrowsRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;




public class RemoveRedundantThrowsTest extends CorrectiveActionTest {
    public RemoveRedundantThrowsTest(String name) {
      super(name);
    }
    
    public static Test suite() {
      return new TestSuite(RemoveRedundantThrowsTest.class);
    }
    
    public String getTemplate() {
      return "Audit/corrective/RedundantThrows/RemoveRedundantThrows/" +
          "<in_out>/";
    }
    
    protected void performSimpleTest() throws Exception {
      super.performSimpleTest(RedundantThrowsRule.class,
          "refactorit.audit.action.redundant_throws.remove_throws");
    }
    
    public void testRemoveRedundantThrows() throws Exception {
      performSimpleTest();
    }
}
