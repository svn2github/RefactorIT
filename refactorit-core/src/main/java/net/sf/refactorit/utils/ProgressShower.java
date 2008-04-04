/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.query.ProgressMonitor.Progress;



/**
 * @author Tonis Vaga
 */
public final class ProgressShower {
  private final Progress progressArea;

  public ProgressShower(int start, int end) {
    progressArea = new ProgressMonitor.Progress(start, end);
  }

  public final void showProgress(int i, int size) {

    ProgressListener listener = (ProgressListener)
        CFlowContext.get(ProgressListener.class.getName());
    if (listener != null) {
      listener.progressHappened(progressArea.getPercentage(i, size));
    }
  }

  public final void showMessage(String str) {

    ProgressListener listener = (ProgressListener)
        CFlowContext.get(ProgressListener.class.getName());
    if (listener != null) {
      listener.showMessage(str);
    }

  }
}
