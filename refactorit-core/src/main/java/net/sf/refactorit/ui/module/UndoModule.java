/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.undo.IUndoManager;
import net.sf.refactorit.refactorings.undo.RitUndoManager;

import java.util.ArrayList;
import java.util.List;


/**
 *
 *
 * @author Tonis Vaga
 */
public class UndoModule {
  public static final boolean UNDO_ENABLED =
      GlobalOptions.getOption("undo.enabled", "true").equals("true");

  private static final UndoAction undoAction = new UndoAction();
  private static final RedoAction redoAction = new RedoAction();

  private static final CreateMilestoneAction createMilestoneAction = new
      CreateMilestoneAction();
  private static final UndoMilestoneAction undoMilestoneAction = new
      UndoMilestoneAction();
  private static final RedoMilestoneAction redoMilestoneAction = new
      RedoMilestoneAction();

  private static final IdeAction[] actions = staticInitActions();
  private static UndoModule instance = new UndoModule();

//static {
//  if (UNDO_ENABLED) {
//    ModuleManager.registerModule(new UndoModule());
//  }
//}

  public static UndoModule getInstance() {
    return instance;
  }

  private static IdeAction[] staticInitActions() {
    IdeAction result[];
    if (Assert.enabled) {
      result = new IdeAction[] {undoAction, redoAction,
          createMilestoneAction,
          undoMilestoneAction, redoMilestoneAction};
    } else {
      result = new IdeAction[] {undoAction, redoAction};
    }

    return result;
  }

  private UndoModule() {}

  public IdeAction[] getAllActions() {
    if (UNDO_ENABLED) {
      return actions;
    }

    return new IdeAction[0];
  }

  public IdeAction[] getAvailableActions() {
    if (UNDO_ENABLED) {
      return getActions();
    }

    return new IdeAction[0];
  }

  private IdeAction[] getActions() {
    IUndoManager manager = RitUndoManager.getInstance();

    List result = new ArrayList(4);
    
    if (manager != null) {
      if (manager.canUndo()) {
        result.add(undoAction);
      }

      if (manager.canRedo()) {
        result.add(redoAction);
      }
    }

//    IMilestoneManager mgr = MilestoneManager
//        .getInstance(IDEController.getInstance().getActiveProject());
//
//    result.add(createMilestoneAction);
//    if ( mgr != null ) {
//      if ( mgr.canUndo() ) {
//        result.add(undoMilestoneAction);
//      } else if ( mgr.canRedo() ) {
//        result.add(redoMilestoneAction);
//      }
//    }

    return (IdeAction[]) result.toArray(new IdeAction[result.size()]);
  }
}
