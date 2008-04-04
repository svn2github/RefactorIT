/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.ui.tree.TitledValue;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;


public class FixmeScannerTreeModel extends DefaultTreeModel {
  private static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(FixmeScannerTreeModel.class);

  private java.util.List fixmeWords;
  private java.util.List compilationUnitsToScan;

  private int startLine;
  private int stopLine;
  private boolean scanAllLines;

  public FixmeScannerTreeModel(java.util.List compilationUnitsToScan,
      List fixmeWords, boolean sortedByTimestampDate, boolean scanAllLines,
      int startLine, int stopLine) {
    super(new DefaultMutableTreeNode(
        new TitledValue(resLocalizedStrings.getString("tree.root"))
        ));

    this.fixmeWords = fixmeWords;

    this.compilationUnitsToScan = compilationUnitsToScan;
    this.scanAllLines = scanAllLines;
    this.startLine = startLine;
    this.stopLine = stopLine;

    populateTree(compilationUnitsToScan, sortedByTimestampDate);
  }

  private void populateTree(java.util.List compilationUnitsToScan,
      boolean sortedByTimestampDate) {
    ViewBuilder view;
    if (sortedByTimestampDate) {
      view = new TimestampOrderedViewBuilder(this);
    } else {
      view = new FileOrderedViewBuilder(this);
    }

    FixmeCommentFinder commentFinder = new FixmeCommentFinder(this.fixmeWords);

    for (int i = 0; i < compilationUnitsToScan.size(); i++) {
      ProgressListener listener = (ProgressListener)
          CFlowContext.get(ProgressListener.class.getName());
      ProgressMonitor.Progress progress = ProgressMonitor.Progress.FULL;

      CompilationUnit compilationUnit = (CompilationUnit) compilationUnitsToScan.get(i);
      Collection fixmesInFile = commentFinder.getFixmeComments(compilationUnit,
          scanAllLines, startLine, stopLine);

      if (fixmesInFile.size() > 0) {
        view.startNewFile(compilationUnit);

        for (Iterator c = fixmesInFile.iterator(); c.hasNext(); ) {
          view.addComment((Comment) c.next());
        }
      }

      if (listener != null) {
        listener.progressHappened(progress.getPercentage(i,
            compilationUnitsToScan.size()));
      }
    }

    view.finish();
  }
}
