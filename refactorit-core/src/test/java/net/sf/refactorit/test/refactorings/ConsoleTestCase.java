/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;


import net.sf.refactorit.ui.RuntimePlatform;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;


public class ConsoleTestCase extends TestCase {

  private PrintStream oldOut;
  private PrintStream oldErr;
  private PrintStream oldConsole;

  private ByteArrayOutputStream out;

  public void setUp() throws Exception {
    oldOut = System.out;
    oldErr = System.err;
    oldConsole = RuntimePlatform.console;

    out = new ByteArrayOutputStream();

    System.setOut(new PrintStream(out));
    System.setErr(new PrintStream(out));

    RuntimePlatform.console = System.out;
  }

  public void tearDown() throws Exception {
    System.setOut(oldOut);
    System.setErr(oldErr);
    RuntimePlatform.console = oldConsole;
  }

  protected String getOut() {
    return new String(out.toByteArray());
  }

  protected void resetOut() {
    out.reset();
  }
}
