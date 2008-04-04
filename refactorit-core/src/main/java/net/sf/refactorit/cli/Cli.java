/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.cli;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.cli.actions.Runner;
import net.sf.refactorit.ui.RuntimePlatform;


/**
 * @author Risto
 */
public class Cli {
  public void run(String[] args) {
    try {
      new ArgumentsValidator().checkUnknownParameters(args);
      run(new StringArrayArguments(args));
    } catch (ProjectInitException e) {
      handleProjectInitException(e);
    }
  }

  private void handleProjectInitException(ProjectInitException e) {
    RuntimePlatform.console.println();
    RuntimePlatform.console.println(e.getMessage());
  }

  public void run(final Arguments arguments) throws ProjectInitException {
    validateArguments(arguments);

    Project project = buildProject(arguments).getProject();
    new Runner().runAction(project, arguments);
  }

  private void validateArguments(final Arguments arguments) throws
      ProjectInitException {

    ArgumentsValidator v = new ArgumentsValidator();
    v.checkArguments(arguments);
    printWarnings(v);
  }

  public void printWarnings(ArgumentsValidator v) {
    for (int i = 0; i < v.getWarnings().size(); i++) {
      RuntimePlatform.console.println(v.getWarnings().get(i));
    }

    RuntimePlatform.console.println();
  }

  private ProjectBuilder buildProject(final Arguments c) throws
      ProjectInitException {

    ProjectBuilder b = new ProjectBuilder(c);
    if (b.hasLoadingErrors()) {
      throw new ProjectInitException(b.getErrorsAsString());
    }

    return b;
  }
}
