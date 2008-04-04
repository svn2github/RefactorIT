/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ProgressListener;



public final class ProgressMonitor extends DelegateVisitor {

  // the progress listener who must be notified about
  // the progress.
  private ProgressListener listener = null;

  // TEMP
  private int temp = 0;
  // Total of sources
  private int totalSources = 0;

  private Progress progress;

  public ProgressMonitor(final ProgressListener listener,
      final Progress progress) {
    // check the preconditions
    if (listener == null) {
      throw new RuntimeException("Preconditions violated in " +
          "ProgressMonitor.ProgressMonitor(..)");
    }

    this.listener = listener;

    this.progress = progress;
  }

  public ProgressMonitor(final ProgressListener listener) {
    this(listener, Progress.FULL);
  }

  public final void visit(final Project project) {
    this.totalSources = project.getCompilationUnits().size();
    //System.out.println("visit(Project project)");
    //System.out.println("Total of sources: "+this.totalSources);
  }

  public final void visit(final BinCIType type) {
    //this.listener.progressHappened(++temp);
  }

  public final void leave(final CompilationUnit source) {
    if (this.totalSources == 0) {
      this.totalSources = 1;
    }
    final int currentTotal = temp++;

    final float percentage = this.progress.getPercentage(currentTotal,
        this.totalSources);
    this.listener.progressHappened(percentage);
//    this.listener.showMessage(Integer.toString(currentTotal));
  }

  public static final class Progress {
    private final float minPercentage;
    private final float maxPercentage;

    public static final Progress FULL = new Progress(0, 100);
    public static final Progress DONT_SHOW = null;

    public Progress(final float minPercentage, final float maxPercentage) {
      this.minPercentage = minPercentage;
      this.maxPercentage = maxPercentage;
    }

    /** @param  index zero-based */
    public final float getPercentage(final int index, final int count) {
      return (maxPercentage - minPercentage) * (index + 1) / count
          + minPercentage;
    }

    public final Progress subdivision(final int index, final int count) {
      return new Progress(getPercentage(index - 1, count), getPercentage(index,
          count));
    }

    public final Progress subdivisionBefore(final int index, final int count) {
      return new Progress(minPercentage, getPercentage(index, count));
    }

    public final Progress subdivisionAfter(final int index, final int count) {
      return new Progress(getPercentage(index, count), maxPercentage);
    }

    public final String toString() {
      return minPercentage + ".." + maxPercentage;
    }
  }
}
