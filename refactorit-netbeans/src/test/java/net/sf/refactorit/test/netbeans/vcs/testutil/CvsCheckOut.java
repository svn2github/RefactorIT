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
import net.sf.refactorit.test.LocalTempFileCreator;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.SourcePathFilter;


public class CvsCheckOut {
  private CvsCommandRunner cvs = new CvsCommandRunner();
  public File dir;

  public CvsCheckOut(String moduleName) throws IOException,
      InterruptedException, NBTestRunnerModule.CancelledException {
    this(new LocalTempFileCreator().createRootDirectory().getFileOrNull(),
        moduleName);
  }

  public CvsCheckOut(File dir, String moduleName) throws IOException,
      InterruptedException, NBTestRunnerModule.CancelledException {
    cvs.exec("checkout -P " + moduleName, dir);

    this.dir = dir.listFiles()[0];
  }

  public void removeAll() throws IOException,
      InterruptedException, NBTestRunnerModule.CancelledException {

    FileUtil.emptyDirectory(dir, new SourcePathFilter());
    cvs.exec("remove", dir);
  }

  public void addDirContents(File dir) throws IOException, InterruptedException,
      NBTestRunnerModule.CancelledException {
    if (dir.getName().equalsIgnoreCase("CVS")) {
      return;
    }

    File[] children = dir.listFiles();

    String addCommand = createParamString(children);

    if (addCommand.equals("")) {
      System.out.println("Note: Nothing to add in " + dir.getAbsolutePath()
          + ", skipping");
    } else {
      cvs.exec("add" + addCommand, dir);
    }

    for (int i = 0; i < children.length; i++) {
      if (children[i].isDirectory()) {
        addDirContents(children[i]);
      }
    }
  }

  private String createParamString(final File[] children) {
    String result = "";

    for (int i = 0; i < children.length; i++) {
      final String param = children[i].getName();

      if (param.indexOf(' ') >= 0) {
        String message = "CVS test code error: should not use files" +
            " or folders with spaces in their names: \"" + param + "\"";

        RitDialog.showMessageDialog(
            IDEController.getInstance().createProjectContext(), message);

        throw new RuntimeException(message);
      }

      if (!param.equals("CVS")) {
        result += " " + param;
      }
    }

    return result;
  }

  public void add(File f) throws IOException, InterruptedException,
      NBTestRunnerModule.CancelledException {
    cvs.exec("add " + f.getName(), f.getParentFile());
  }

  public void addBinary(File f) throws IOException, InterruptedException,
      NBTestRunnerModule.CancelledException {
    cvs.exec("add -kb " + f.getName(), f.getParentFile());
  }

  public void commit() throws IOException, InterruptedException,
      NBTestRunnerModule.CancelledException {
    cvs.exec("commit -m auto-exec", dir);
  }
}
