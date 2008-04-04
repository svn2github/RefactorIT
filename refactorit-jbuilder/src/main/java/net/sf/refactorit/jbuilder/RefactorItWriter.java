/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.MessageCategory;
import com.borland.primetime.ide.MessageView;

import java.io.IOException;
import java.io.Writer;


/**
 * Writes all messages into JBuilder output window.
 *
 * @author Vladislav Vislogubov
 */
public class RefactorItWriter extends Writer {
  private static final MessageCategory CATEGORY =
      new MessageCategory("RefactorIT");

  private MessageView mv = Browser.getActiveBrowser().getMessageView();
  private StringBuffer buffer = new StringBuffer();

  public void write(int ch) throws IOException {
    if (ch == '\n') {
      mv.addMessage(CATEGORY, buffer.toString());
      buffer.setLength(0);
    } else {
      buffer.append(ch);
    }
  }

  public void write(char[] buf, int off, int len) throws IOException {
    len += off;

    for (int i = off; i < len; i++) {
      if (buf[i] == '\n') {
        int n = i - off;
        if (n > 0) {
          buffer.append(buf, off, n);
        }
        off = i + 1;

        mv.addMessage(CATEGORY, buffer.toString());
        buffer.setLength(0);
      }
    }

    if (off < len) {
      buffer.append(buf, off, len - off);
    }
  }

  public void flush() throws IOException {}

  public void close() throws IOException {}
}
