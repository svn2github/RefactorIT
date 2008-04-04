/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.vfs.ClassPath;
import net.sf.refactorit.vfs.SourcePath;

import java.util.List;

/**
 * WorkspaceManager class for managing workspace projects
 *
 * Concepts -
 * 1. IDE project -- IDE project, for Eclipse IProject, For Jdev - JProject etc
 * 2. Project identificator -- string, we should able to identificate projects by string
 *   create mapping in subclasses when needed.
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.13 $ $Date: 2005/12/09 12:02:59 $
 */
public abstract class WorkspaceManager {
  protected Workspace workspace;

  /**
   * @return Returns the workspace.
   */
  public abstract Workspace getWorkspace();

  /**
   * @param projectIdent -- project identificator
   * @return IDE project corresponding to identificator
   */
  protected abstract Object getIdeProject(String projectIdent);

  /**
   * Returns IDE projects the given project depends on
   * @param projectIdentificator
   */
  public abstract List getDependsOnProjects(Object projectIdentificator);

  public abstract List getReferencedInProjects(Object projectIdentificator);


  protected boolean checkClassPathSanity(
      ClassPath classpath, boolean showDialogIfNeeded
  ) {
    if (!classpath.contains("java/lang/Object.class")) {
// XXX: remove?
//      if (showDialogIfNeeded) {
//        SwingUtilities.invokeLater(new Runnable() {
//          public void run() {
//            ///SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions() ?
//            /*errors.add(
//                "RefactorIT: Please fix your classpath (under" +
//                " \"RefactorIT Project Options\"), it does not" +
//                " contain java.lang.Object");
//            Title: "classpath error"*/
//          }
//        });
//      }

      return false;
    }

    return true;
  }

  protected boolean checkSourcePathSanity(SourcePath srcPath, boolean showDialogIfNeeded) {
    if ( srcPath.getRootSources().length == 0) {
//      if (showDialogIfNeeded) {
//        SwingUtilities.invokeLater(new Runnable() {
//          public void run() {
//            /*RitDialog.showMessageDialog(
//                createProjectContext(),
//                "RefactorIT: Please fix your sourcepath (under" +
//                " \"RefactorIT Project Options\"), it is currently empty!",
//                "Sourcepath error", JOptionPane.ERROR_MESSAGE);*/ // XXX
//          }
//        });
//      }
      return false;
    }

    return true;
  }

  public abstract Object getIdeProjectIdentifier(Object ideProject);

  public abstract Object getProjectIdentifier(Project activeProject);

  public void clear() {
  }
}
