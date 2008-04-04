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
import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.refactorings.undo.MilestoneManager;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.ShortcutKeyStrokes;

import javax.swing.KeyStroke;

import java.io.File;


/**
 * @author Tonis Vaga
 */
public class CreateMilestoneAction extends AbstractIdeAction implements
    ShortcutAction {
  public static final String KEY = "refactorit.action.CreateMilestoneAction";

  public static final String DIR
      = RuntimePlatform.getConfigDir() + File.separatorChar + "milestone";

  public KeyStroke getKeyStroke() {
    return ShortcutKeyStrokes.getByKey(CreateMilestoneAction.KEY);
  }

  public String getName() {
    return "Create Checkpoint";
  }

  public String getKey() {
    return KEY;
  }

//  public boolean isReadonly() { return true; }
//
//  public boolean isMultiTargetsSupported() {
//    return false;
//  }

  public boolean run(IdeWindowContext context) {
    IDEController controller = IDEController.getInstance();
    controller.ensureProject();

    Project project = controller.getActiveProject();
    if (project == null) {
      DialogManager.getInstance().showWarning(context,
          "warning.createmilestone.projectnull");
      return false;
    }

    MilestoneManager.getInstance(project).createMilestone();

    return false;
  }

  public char getMnemonic() {
    return (char) 0;
  }
}
