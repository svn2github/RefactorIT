/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vcs.testutil;


import java.io.File;
import java.io.IOException;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.testmodule.NBTestRunnerModule;
import net.sf.refactorit.ui.dialog.RitDialog;


public class CvsCommandRunner {
  private CommandLineRunner cmdLine = new CommandLineRunner();

  public void exec(String command, File dir)
  throws
      IOException, InterruptedException,
      NBTestRunnerModule.CancelledException 
  {
    int result = cmdLine.run("cvs -d " +
        NBTestRunnerModule.Parameters.getCvsRoot() + " " + command, dir);

    if (result != 0) {
      RitDialog.showMessageDialog(
          IDEController.getInstance().createProjectContext(),
          "CVS command failed (exit value " + result + "); see logs");
    }
  }
}
