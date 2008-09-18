/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse.vfs;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.commonIDE.options.PathItem;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;
import net.sf.refactorit.vfs.AbstractClassPath;
import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.local.LocalClassPath;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Tonis Vaga
 *
 * TODO: test that resolving classpath entries outside of project is correct
 * TODO: test that resolving  entries describing folder for .class files are correct
 */
public class EclipseClassPath extends AbstractClassPath {
  private static final Logger log = Logger.getLogger(EclipseClassPath.class);

  private static final boolean debug = false;

  private IProject project;

  private ProjectOptions options;

  public EclipseClassPath(IProject project, ProjectOptions projectOptions) {
    this.project = project;
    this.options = projectOptions;

    if (project != null && JavaCore.create(project) == null) {
      log.warn("Not java project " + project.getName());

      project = null;
    }
  }

  public ClassPathElement[] createElements() {
    if (project == null || !project.isOpen()) {
      return new ClassPathElement[0];
    }

    ArrayList result;

    if (options.isAutoDetect()) {
      IJavaProject jProject = JavaCore.create(project);
      try {
        IClasspathEntry[] pathEntries = jProject.getResolvedClasspath(true);
        result = new ArrayList(pathEntries.length);

        for (int i = 0; i < pathEntries.length; i++) {
          IClasspathEntry entry = pathEntries[i];
          if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
            IPath path = entry.getPath();

            if (debug) {
              log.debug("creating classpath entry for path " + path);
            }

            File file = path.toFile();

            if (!file.exists()) {
              // FIXME: hack, improve
              // lib under workspace
              IFile f = project.getWorkspace().getRoot().getFile(path);
              file = f.getRawLocation().toFile();
            }

            addClasspathElement(result, file);
          }
        }
      } catch (JavaModelException e) {
        throw new SystemException(
            ErrorCodes.ECLIPSE_INTERNAL_ERROR,
            "resolving classpath failed", e);
      }
    } else {
      log.debug("creating customized classpath elements");

      PathItem[] items = options.getClassPath().getItems();
      result = new ArrayList(items.length);

      for (int i = 0; i < items.length; i++) {
        File file = new File(items[i].getAbsolutePath());
        addClasspathElement(result, file);
      }
    }

    return (ClassPathElement[]) result
        .toArray(new ClassPathElement[result.size()]);
  }

  private void addClasspathElement(List result, File file) {
    if (!file.exists()) {
      log.warn("classpath element " +
          file.getAbsolutePath() + " doesn't exist");
    }

    ClassPathElement element = LocalClassPath.createClassPathElement(file);
    if (element != null) {
      result.add(element);
    }
  }

  public void setOptions(ProjectOptions projectOptions) {
    this.options = projectOptions;
  }
}
