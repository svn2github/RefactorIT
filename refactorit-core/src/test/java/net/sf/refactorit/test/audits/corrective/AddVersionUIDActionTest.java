/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.serialization.SerialVersionUIDRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author  Arseni Grigorjev
 */
public class AddVersionUIDActionTest extends CorrectiveActionTest{
  
  public AddVersionUIDActionTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(AddVersionUIDActionTest.class);
  }
  
  public String getTemplate() {
    return "Audit/corrective/SerialVersionUID/AddSerialVersionUIDAction/" +
        "<in_out>/<test_name>.java";
  }
  
  /************************ Simple Tests ********************************/

  protected void performSimpleTest() throws Exception {
    super.performSimpleTest(SerialVersionUIDRule.class,
        "refactorit.audit.action.serial_version_uid.add");
  }

  public void testAddField() throws Exception {
    performSimpleTest();
  }
    
}
