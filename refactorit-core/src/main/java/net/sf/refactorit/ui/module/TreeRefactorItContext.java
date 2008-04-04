/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.ui.ParsingMessageDialog;
import net.sf.refactorit.ui.dialog.ContextWrapper;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.errors.ErrorsTab;
import net.sf.refactorit.ui.panel.ResultsTreeDisplayState;
import net.sf.refactorit.ui.panel.RowHideState;
import net.sf.refactorit.utils.ParsingInterruptedException;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;


public abstract class TreeRefactorItContext
  implements RefactorItContext, Cloneable
{
  private ResultsTreeDisplayState resultsTreeDisplayState
              = new ResultsTreeDisplayState();

  private RowHideState rowHideState = new RowHideState();

  public ResultsTreeDisplayState getResultsTreeDisplayState() {
    return this.resultsTreeDisplayState;
  }

  public RowHideState getRowHideState() {
    return this.rowHideState;
  }

  /**
   * NPE when hiding of root node attemtped
   */
  public void hideSelectedRow(JTree tree) {
    this.rowHideState.hideSelectedRow(tree);
  }

  public void restoreRowHideState(JTree tree) {
    rowHideState.restoreHideState(getProject(), tree);
  }

  public void saveRowHideState(TreeModel model) {
    rowHideState.saveHideState(model);
  }

  public void hideHiddenRows(JTree tree) {
    this.resultsTreeDisplayState.saveExpansionAndScrollState(tree);
    this.rowHideState.hideHiddenRows(tree.getModel());
    this.resultsTreeDisplayState.restoreExpansionAndScrollState(tree,
        getProject());
  }

  public void showHiddenRows(JTree tree) {
    this.resultsTreeDisplayState.saveExpansionAndScrollState(tree);
    this.rowHideState.showHiddenRows(tree.getModel());
    this.resultsTreeDisplayState.restoreExpansionAndScrollState(tree,
        getProject());
  }

  public boolean selectedNodeCanBeHidden(JTree tree) {
    return this.rowHideState.selectedNodeCanBeHidden(tree);
  }

  public boolean hiddenNodesVisible() {
    return this.rowHideState.isShowHiddenRows();
  }

  public void reset() {
    rowHideState.reset();
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#getWindowId()
   */
  public String getWindowId() {
    return null;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#copy()
   */
  public RefactorItContext copy() {
    try {
      // FIXME: shouldn't state variables be also cloned?
      return (TreeRefactorItContext) clone();
    } catch (CloneNotSupportedException e) {
      throw new ChainableRuntimeException(
          "Cloning is not supported for this instance: "
          + this.getClass(), e);
    }
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#copy(net.sf.refactorit.ui.dialog.RitDialog)
   */
  public IdeWindowContext copy(RitDialog owner) {
    return new ContextWrapper(this, owner);
  }

  public void rebuildAndUpdateEnvironment() {
    final ParsingMessageDialog dlg = new ParsingMessageDialog(this);
    dlg.setDialogTask(
        new ParsingMessageDialog.RebuildProjectTask(this.getProject()));
    try {
      dlg.show(true);
    } catch (ParsingInterruptedException ex) {
      return;
    }

    this.reload();

    ErrorsTab.addNew(this);
  }

  /** This is some legacy hack for JB */
  public void postponeShowUntilNotified() {
  }

  /** This is some legacy hack for JB */
  public void showPostponedShows() {
  }
}
