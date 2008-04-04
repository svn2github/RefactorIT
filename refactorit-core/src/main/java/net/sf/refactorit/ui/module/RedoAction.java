/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.refactorings.undo.IUndoManager;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.ShortcutKeyStrokes;

import javax.swing.KeyStroke;


/**
 * @author Tonis Vaga
 */
public class RedoAction extends AbstractIdeAction implements ShortcutAction {
  public static final String KEY = "refactorit.action.RedoAction";
  public static final String NAME = "Redo";

  public RedoAction() {
  }

  public KeyStroke getKeyStroke() {
    return ShortcutKeyStrokes.getByKey(RedoAction.KEY);
  }

  public String getName() {
    javax.swing.undo.UndoManager manager = RitUndoManager.getInstance();
    if (manager == null) {
      return NAME;
    }

    return manager.getRedoPresentationName();
  }

  public String getKey() {
    return KEY;
  }

//  public boolean isReadonly() { return false; }

  public boolean isAvailable() {
    IUndoManager manager = RitUndoManager
        .getInstance(IDEController.getInstance().getActiveProject());

    return (manager != null && manager.canRedo());
  }

  public boolean run(IdeWindowContext context) {
    if (!isAvailable()) {
      return false;
    }

    IUndoManager manager = RitUndoManager
        .getInstance(IDEController.getInstance().getActiveProject());
    IDEController controller = IDEController.getInstance();

    DialogManager dlgMgr = DialogManager.getInstance();

    int result = dlgMgr.showYesNoHelpQuestion(context,
        "question.undo", "Do you really want to redo refactoring " +
        manager.getPresentationNameWIthDetails(false) + " ?", "refact.undo");

    if (result != DialogManager.YES_BUTTON) {
      return false;
    }

    try {
      controller.saveAllFiles();
      try {
        JProgressDialog.run(context, new Runnable() {
          public void run() {
            RitUndoManager.getInstance(
                IDEController.getInstance().getActiveProject()).redo();
          }
        }


        , "Redo ...", false);
      } catch (SearchingInterruptedException e) {
      }

    } catch (javax.swing.undo.CannotRedoException ex) {
      ex.printStackTrace();
    }

    return true;
  }

//  public boolean isMultiTargetsSupported() {
//    return false;
//  }

  public char getMnemonic() {
    return 'E';
  }
}
