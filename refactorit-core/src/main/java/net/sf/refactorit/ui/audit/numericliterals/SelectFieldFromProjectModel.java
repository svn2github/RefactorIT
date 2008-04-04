/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit.numericliterals;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.StringUtil;
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
public class SelectFieldFromProjectModel extends BinTreeTableModel {
  private boolean buildCorrectly = true;
  private BinTypeRef returnType;

  BinTypeRef wantToAccessFrom;
  
  public SelectFieldFromProjectModel(final BinTypeRef wantToAccessFrom,
      BinTypeRef returnType) {
    super(new BinTreeTableNode("Project"));
    this.returnType = returnType;
    this.wantToAccessFrom = wantToAccessFrom;
  }
  
  /**
   * @param context Context for showing Progress Dialog
   */
  public void buildProjectTree(final IdeWindowContext context) {
    final SelectFieldFromProjectVisitor visitor 
        = new SelectFieldFromProjectVisitor(this, wantToAccessFrom, 
        returnType);
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
  
  public Class getColumnClass(int column) {
    switch (column){
      case 0: return TreeTableModel.class;
      case 1: return String.class;
      default: return String.class;
    }
  }
  
  public int getColumnCount() {
    return 2;
  }
  
  public String getColumnName(final int column) {
    switch (column){
      case 0: return "Location";
      case 1: return "Field value";
      default: return StringUtil.EMPTY_STRING;
    }
  }
  
  public Object getValueAt(Object node, int column){
    if (node instanceof BinTreeTableNode){
      switch (column){
        case 0: 
          return node;
        case 1: 
          if (((BinTreeTableNode) node).getBin() instanceof BinField){
            return ((BinField) ((BinTreeTableNode) node).getBin())
                .getExpression().getText();
          }
        default: 
          return StringUtil.EMPTY_STRING;
      }
    }
    
    return StringUtil.EMPTY_STRING;
  }
  
  public boolean isBuildCorrectly() {
    return this.buildCorrectly;
  }
  
}
