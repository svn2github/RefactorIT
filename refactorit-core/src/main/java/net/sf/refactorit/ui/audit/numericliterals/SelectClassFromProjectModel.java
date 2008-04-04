/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit.numericliterals;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.TreeTableModel;

/**
 *
 * @author Arseni Grigorjev
 */
public class SelectClassFromProjectModel extends BinTreeTableModel {
  private boolean buildCorrectly = true;
  
  public SelectClassFromProjectModel(final BinTypeRef wantToAccessFrom,
      final IdeWindowContext context) {
    super(new BinTreeTableNode("Project"));
    final SelectClassFromProjectVisitor visitor 
        = new SelectClassFromProjectVisitor(this, wantToAccessFrom);
    
    try {
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          AbstractIndexer.runWithProgress(ProgressMonitor.Progress.FULL,
              new Runnable() {
            public void run() {
              wantToAccessFrom.getProject().accept(visitor);
            }
          });
        }
      }

      , true);
    } catch (SearchingInterruptedException e){
      ((BinTreeTableNode) getRoot()).removeAllChildren();
      ((BinTreeTableNode) getRoot()).addChild(new BinTreeTableNode(
          "Project search was interrupted."));
      buildCorrectly = false;
    }
  }
  
  public int getColumnCount() {
    return 1;
  }
  
  public String getColumnName(final int column) {
    return "Location";
  }
  
  public Class getColumnClass(final int column) {
    return TreeTableModel.class;
  }
  
  public Object getValueAt(Object node, int column){
    return node;
  }

  public boolean isBuildCorrectly() {
    return this.buildCorrectly;
  }
}
