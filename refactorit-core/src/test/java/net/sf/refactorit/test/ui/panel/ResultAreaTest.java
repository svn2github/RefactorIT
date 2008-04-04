/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.ui.panel;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.test.commonIDE.NullController;
import net.sf.refactorit.ui.panel.ResultArea;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author risto
 */
public class ResultAreaTest extends TestCase {
  public ResultAreaTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(ResultAreaTest.class);
  }

  public void testExceptionOnMenuAction() {
    final StringBuffer log = new StringBuffer();

    final IDEController old = IDEController.getInstance();
    IDEController.setInstance(new NullController() {
      public void showAndLogInternalError(Exception ex) {
        log.append("Called with: " + ex.getClass().getName() + " ");
      }
    } );

    try {
      ResultArea.onMenuClick(null, null, null);
    } finally {
      IDEController.setInstance(old);
    }

    assertEquals("Called with: java.lang.NullPointerException ",
        log.toString());
  }

}
