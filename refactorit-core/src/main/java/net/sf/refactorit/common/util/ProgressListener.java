/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

public interface ProgressListener {
  public final class SilentListener implements ProgressListener {
    public final void progressHappened(float persentage) {
    }

    public final void showMessage(String message) {
    }
  }


  ProgressListener SILENT_LISTENER = new SilentListener();

  void progressHappened(float percentage);

  void showMessage(String message);
}
