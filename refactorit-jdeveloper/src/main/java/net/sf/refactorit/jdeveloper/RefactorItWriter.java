/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;

import oracle.ide.layout.ViewId;
import oracle.ide.log.MessagePage;

import java.io.IOException;
import java.io.Writer;


/**
 * A writer that writes all messages to RefactorIT log window
 *
 * @author  Tanel
 */
public class RefactorItWriter extends Writer {
  static class RefactorItLogPage extends MessagePage {
    protected static final String VIEW_ID = "RefactorITLogPage";

    /**
     * Creates a log page
     *
     * @param pageName the name to be shown on the page's tab.
     */
    public RefactorItLogPage(String pageName) {
      super(new ViewId(VIEW_ID, pageName));
    }

    /**
     * Prints text in the log window.
     *
     * @param message the text to be displayed.
     */
    protected void logMsg(String message) {
      super.logMsg(message);
    }
  }


  private static RefactorItLogPage logger = new RefactorItLogPage("RefactorIT");

  public void write(int ch) throws IOException {
    logMessage(String.valueOf(ch));
  }

  public void write(char[] buf, int off, int len) throws IOException {
    logMessage(String.valueOf(buf, off, len));
  }

  public void flush() throws IOException {
  }

  public void close() throws IOException {
  }

  private static void logMessage(String message) {
    logger.show();
    logger.logMsg(message);
  }
}
