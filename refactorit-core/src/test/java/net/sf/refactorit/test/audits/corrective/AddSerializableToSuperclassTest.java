/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.serialization.NotSerializableSuperRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author  Arseni Grigorjev
 */
public class AddSerializableToSuperclassTest extends CorrectiveActionTest{
  
  public AddSerializableToSuperclassTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(AddSerializableToSuperclassTest.class);
  }
  
  public String getTemplate() {
    return "Audit/corrective/NotSerializableSuperclass/" +
        "AddSerializableToSuperclass/<in_out>/<test_name>.java";
  }
  
  /************************ Simple Tests ********************************/

  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(NotSerializableSuperRule.class,
        "refactorit.audit.action.not_serializable_super.add_serializable");
  }

  public void test1() throws Exception {
    performSimpleTest();
  }
    
}
