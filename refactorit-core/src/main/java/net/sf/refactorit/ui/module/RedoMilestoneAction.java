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



/**
 * @author Tonis Vaga
 */
public class RedoMilestoneAction extends AbstractIdeAction {
  public String getKey() {
    return "refactorit.action.RedoMilestoneAction";
  }

  public String getName() {
    IMilestoneManager instance = MilestoneManager
        .getInstance(IDEController.getInstance().getActiveProject());

    final String baseName = "Redo Checkpoint";

    if (instance == null || !instance.canRedo()) {
      return baseName;
    }

    return baseName + " " + instance.getMilestoneInfo();
  }

  public boolean isAvailable() {
    IMilestoneManager mgr = MilestoneManager
        .getInstance(IDEController.getInstance().getActiveProject());

    return (mgr != null && mgr.canRedo());
  }

  public boolean run(IdeWindowContext context) {
    if (!isAvailable()) {
      return false;
    }

    Project project = IDEController.getInstance().getActiveProject();
    IMilestoneManager mgr = MilestoneManager.getInstance(project);

    mgr.redo();
    project.getProjectLoader().markProjectForRebuild();

    return true;
  }

  public boolean needsEnsureProject() {
    return true;
  }

  public char getMnemonic() {
    return (char) 0;
  }
}
