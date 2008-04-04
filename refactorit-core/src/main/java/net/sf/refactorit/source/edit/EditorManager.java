/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;



import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.SourcesModificationOperation;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.source.preview.ChangesPreviewModel;
import net.sf.refactorit.ui.DialogManager;

import javax.swing.undo.CannotUndoException;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class EditorManager {
  static final boolean debug = false;

  private List editors = new ArrayList();

  private ProgressMonitor.Progress progressArea;

  private boolean editCanceled;

  private LineManager lineManager;
  private boolean persistantLineManager = false;

  RefactoringStatus status;

  private boolean showPreview;
  private String refactoringName = null;
  private String description = null;

  public EditorManager(boolean showPreview, String refactoringName) {
    this.showPreview = showPreview;
    this.refactoringName = refactoringName;
  }

  public EditorManager(boolean showPreview, String refactoringName, String description) {
    this(showPreview, refactoringName);
    this.description = description;
  }

  public void setShowPreview(final boolean showPreview){
    this.showPreview = showPreview;
  }

  public void addEditor(Editor editor) {
    if (Assert.enabled) {
      Assert.must(editor != null, "Was given null editor");
    }

    CollectionUtil.addNew(this.editors, editor);
  }

  public RefactoringStatus performEdit() {
    if (lineManager == null || !persistantLineManager){
      lineManager = new LineManager();
    }
    status = new RefactoringStatus();

    mergeModifierEditors();

    int max = this.editors.size();
    for (int i = 0; i < max; i++) {
      Editor editor = (Editor)this.editors.get(i);

      if (debug) {
        System.err.println("Applying: " + editor);
      }
      RefactoringStatus editorStatus = editor.apply(lineManager);
      status.merge(editorStatus);

      if (status.getSeverity() == RefactoringStatus.FATAL) {
        showProgress(max - 1, max);
        lineManager.clear();
        return status;
      }

      showProgress(i * 2 / 3, max);
    }

    if (debug) {
      lineManager.dumpAllSources(System.err);
    }

    status.merge(lineManager.canWrite());

    // commit happens only if there were absolutely no logic errors detected
    if (!status.isErrorOrFatal()) {
     // final boolean showPreview = (refactoring != null &&
     //     refactoring.isUsingDefaultChangesPreview() &&
     //     IDEController.getInstance().getPlatform() != IDEController.TEST);

      if (showPreview) {
        ChangesPreviewModel model = lineManager.getPreviewModel();

        boolean okPressed = DialogManager.getInstance().showConfirmations(
            (refactoringName != null ? "<" + refactoringName + "> " : "") +
            "Changes Preview",
            "The following changes are necessary to perform the refactoring",
            model,
            IDEController.getInstance().createProjectContext(),
            description,
            "refact.preview");

        if (!okPressed) {
          editCanceled = true;
          status.addEntry("", RefactoringStatus.CANCEL);
        } else if (!model.isAllNodesSelected()) {
          lineManager.checkUserInput(model);
        }
      }

      if (!editCanceled) {
        // FIXME should stop on the first exception and rollback all files
        SourcesModificationOperation modificationOp = new SourcesModificationOperation() {
          protected void runImpl() {
              status.merge(lineManager.writeSources());
          }
        };

        IDEController.getInstance().run(modificationOp);

        if (modificationOp.getException() != null) {
          throw new SystemException(ErrorCodes.INTERNAL_ERROR,
              "Exception during editing sources", modificationOp.getException());
        }
      } else if (persistantLineManager) {
        RitUndoManager.getCurrentTransaction().end();
        try {
          RitUndoManager.getCurrentTransaction().undo();
        } catch (CannotUndoException e){
          AppRegistry.getLogger(getClass()).warn(
              "Was unable to perform UNDO action after Cancel command.");
        }
      }
    }

    showProgress(max - 1, max);

    if (!persistantLineManager) {
      lineManager.clear();
    } else {
      lineManager.clearFilesystemEditors();
    }

    return status;
  }

//  private String errorToString(final Object e) {
//    String cur = null;
//    if (e instanceof Exception) {
//      cur = ((Exception) e).getMessage();
//      if (cur == null || cur.length() == 0) {
//        cur = e.toString();
//      }
//    } else if (e instanceof String) {
//      cur = (String) e;
//    } else {
//      if (Assert.enabled) {
//        Assert.must(false, "Strange logic error object: "
//            + e.getClass().getName());
//      }
//    }
//
//    return cur;
//  }

 // public boolean isContainsEditors() {
 //   return getEditorsCount() > 0;
 // }

  public int getEditorsCount() {
    return this.editors.size();
  }

  public void setProgressArea(ProgressMonitor.Progress progressArea) {
    this.progressArea = progressArea;
  }

  protected void showProgress(int i, int size) {
    if (progressArea == null) {
      progressArea = new ProgressMonitor.Progress(0, 100);
    }

    ProgressListener listener = (ProgressListener)
        CFlowContext.get(ProgressListener.class.getName());

    if (listener != null) {
      //listener.);
      listener.progressHappened(progressArea.getPercentage(i, size));
    }
  }

  public void mergeModifierEditors() {
    for (int i = 0; i < editors.size(); i++) {
      if (editors.get(i) instanceof ModifierEditor) {
        ModifierEditor editor = (ModifierEditor) editors.get(i);

        for (int j = i + 1; j < editors.size(); ) {
          if (editors.get(j) instanceof ModifierEditor) {
            ModifierEditor editorToMerge = (ModifierEditor) editors.get(j);
            if (editor.getMember().equals(editorToMerge.getMember())) {
              editor.merge(editorToMerge);
              editors.remove(j);
              continue;
            }
          }
          ++j;
        }
      }
    }
  }

  public LineManager getLineManager() {
    return this.lineManager;
  }

  public void setLineManager(final LineManager manager) {
    this.lineManager = manager;
  }

  public boolean isPersistantLineManager() {
    return this.persistantLineManager;
  }

  public void setPersistantLineManager(final boolean persistantLineManager) {
    this.persistantLineManager = persistantLineManager;
  }
}
