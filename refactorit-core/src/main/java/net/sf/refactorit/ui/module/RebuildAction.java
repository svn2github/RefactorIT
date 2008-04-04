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
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.ui.DialogManager;




/**
 * @author Tonis Vaga
 */
public class RebuildAction extends AbstractIdeAction {
  public static final String KEY = "refactorit.action.RebuildAction";

  public String getName() {
    return "Rebuild Cache";
  }

  public String getKey() {
    return KEY;
  }

  public char getMnemonic() {
    return 'R';
  }

  public boolean run(IdeWindowContext context) {
    IDEController instance = IDEController.getInstance();
    Project project = instance.getActiveProject();

    if (project != null) {
      project.getProjectLoader().markProjectForRebuild();

      // Need to ensure here before the "success" dialog is shown
      if (IDEController.runningTest()) {
        IDEController.getInstance().ensureProject(new LoadingProperties(false));
      } else {
        IDEController.getInstance().ensureProject();
      }
    } else {
      AppRegistry.getLogger(this.getClass()).debug("cleanup called when project==null");
    }

    DialogManager.getInstance().showInformation(context,
        "rebuild.done", "Rebuild Complete");

//  instance.ensureProject();
    return true;
  }
}
