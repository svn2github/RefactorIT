/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.undo.IMilestoneManager;
import net.sf.refactorit.refactorings.undo.MilestoneManager;
import net.sf.refactorit.ui.DialogManager;



public class UndoMilestoneAction extends AbstractIdeAction {
  public static final String KEY = "refactorit.action.RestoreMilestoneAction";

  public boolean needsEnsureProject() {
    return true;
  }

  public String getName() {
    IMilestoneManager instance = MilestoneManager
        .getInstance(IDEController.getInstance().getActiveProject());

    final String baseName = "Undo Checkpoint";

    if (instance == null || !instance.canUndo()) {
      return baseName;
    }

    return baseName + " " + instance.getMilestoneInfo();
  }

  public String getKey() {
    return KEY;
  }

  public char getMnemonic() {
    return (char) 0;
  }

  public boolean isAvailable() {
    IMilestoneManager mgr = MilestoneManager.getInstance(IDEController.
        getInstance().getActiveProject());

    return (mgr != null && mgr.canUndo());
  }

//  public boolean isReadonly() {
//    return false;
//  }

//  public boolean isMultiTargetsSupported() {
//    return false;
//  }

  public boolean run(IdeWindowContext context) {
    if (!isAvailable()) {
      return false;
    }

    IDEController controller = IDEController.getInstance();
    Project project = controller.getActiveProject();
    if (project == null) {
      DialogManager.getInstance().showWarning(context,
          "warning.createmilestone.projectnull");
      return false;
    }

    IMilestoneManager mgr = MilestoneManager.getInstance(project);

    if (!mgr.canUndo()) {
      return false;
    }

    mgr.undo();

    project.getProjectLoader().markProjectForCleanup();

    return true;
  }
}
