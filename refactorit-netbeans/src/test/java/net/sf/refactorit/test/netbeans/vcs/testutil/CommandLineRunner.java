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
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.utils.FileUtil;


public class CommandLineRunner {
  public int run(String command, File dir) throws IOException,
      InterruptedException {
    System.out.println();
    System.out.println(dir + "> " + command);

    Process process = Runtime.getRuntime().exec(command, null, dir);

    startPumpThread(process.getInputStream(), System.out);
    startPumpThread(process.getErrorStream(), System.out);

    process.waitFor();

    return process.exitValue();
  }

  private void startPumpThread(final InputStream inputStream,
      final OutputStream outputStream) {
    new Thread(new Runnable() {
      public void run() {
        try {
          FileCopier.pump(inputStream, outputStream, 1, true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
