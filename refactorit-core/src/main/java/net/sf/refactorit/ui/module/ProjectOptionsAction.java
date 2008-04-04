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
import net.sf.refactorit.commonIDE.options.ProjectOptionsDialog;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;



public class ProjectOptionsAction extends AbstractIdeAction {
  public static final String KEY = "refactorit.action.ProjectOptionsAction";

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return "Project Options";
  }

  public char getMnemonic() {
    return (char) 0;
  }

  public boolean needsEnsureProject() {
    return true;
  }

  public boolean run(IdeWindowContext context) {
    IDEController controller = IDEController.getInstance();

    Project project = controller.getActiveProject();

    ProjectOptionsDialog dialog = new ProjectOptionsDialog(project);

    dialog.show();

    if (!dialog.isOkPressed()) {
      return false;
    }

    // fixme: bad design
    dialog.updateSettings();
    saveOptions(project.getOptions());

    project.fireProjectSettingsChangedEvent();

    // TODO: actually we may need to recreate project
    // if sourcepath or classpath needs recreating
    if ("true".equals(
        GlobalOptions.getOption("rebuild.project.options.change", "true"))) {
      controller.ensureProject();

      controller.checkSourcePathSanity(project.getPaths().getSourcePath(), true);
      controller.checkClassPathSanity(project.getPaths().getClassPath(), true);
    }

    return false;
  }

  private static void saveOptions(ProjectOptions options) {
    AppRegistry.getLogger(ProjectOptionsAction.class)
        .debug("Options are " + options);

    options.serialize();
  }
}
