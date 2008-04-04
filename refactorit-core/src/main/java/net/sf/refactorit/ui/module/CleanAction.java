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
import net.sf.refactorit.ui.DialogManager;




/**
 * @author Tonis Vaga
 */
public class CleanAction extends AbstractIdeAction {
  public static final String KEY = "refactorit.action.CleanAction";

  public String getName() {
    return "Clean Cache";
  }

  public String getKey() {
    return KEY;
  }

  public char getMnemonic() {
    return 'C';
  }

  public boolean run(IdeWindowContext context) {
    Project project = IDEController.getInstance().getActiveProject();

    if (project != null) {
      project.getProjectLoader().markProjectForCleanup();
    } else {
      AppRegistry.getLogger(this.getClass()).debug("cleanup called when project==null");
    }
    
    DialogManager.getInstance().showInformation(context, 
        "rebuild.done", "Clean Complete");
            
    return false;
  }
}
