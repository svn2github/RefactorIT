/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

// java classes
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 */
public final class StreamGobbler extends Thread {
  final InputStream is;
  final String type;

  public StreamGobbler(InputStream is, String type) {
    this.is = is;
    this.type = type;
  }

  public final void run() {
    try {
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      String line = null;
      while ((line = br.readLine()) != null) {
        System.out.println(type + ">" + line);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
