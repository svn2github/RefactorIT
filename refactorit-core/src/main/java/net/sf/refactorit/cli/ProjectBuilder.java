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
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.test.commonIDE.MockWorkspace;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.utils.ClasspathUtil;
import net.sf.refactorit.vfs.local.LocalClassPath;
import net.sf.refactorit.vfs.local.LocalSourcePath;

import java.util.Iterator;


/**
 * @author RISTO A
 */
public class ProjectBuilder {
  private Project p;

  /*public ProjectBuilder(Arguments cl) {
    this(cl.getSourcepath(), cl.getClasspath());
  }*/

  public ProjectBuilder(Arguments cl) {
    try {
      MockWorkspace space = (MockWorkspace) IDEController.getInstance().getWorkspace();
      space.addIdeProject(cl);
      p = space.getProject(cl);
      p.getProjectLoader().build(null, false);
    } catch (Exception e) {
      e.printStackTrace(RuntimePlatform.console);
    }
  }

  public ProjectBuilder(String sourcepath, String classpath) {
    try {
      p = new Project("",
          new LocalSourcePath(sourcepath),
          new LocalClassPath(getClasspathWithDefaultEntries(classpath)), null);
      p.getProjectLoader().build(null, false);

    } catch (Exception e) {
      e.printStackTrace(RuntimePlatform.console);
    }
  }

  public Project getProject() {
    return p;
  }

  public String getErrorsAsString() {
    if (p.getCompilationUnits().size() == 0) {
      return "ERROR: Sourcepath is empty";
    }

    return collectErrors(p);
  }

  public boolean hasLoadingErrors() {
    return!getErrorsAsString().equals("");
  }

  // Util methods

  public static String getClasspathWithDefaultEntries(String classpath) {
    return classpath + StringUtil.PATH_SEPARATOR +
        ClasspathUtil.getDefaultClasspath();
  }

  public static String collectErrors(final Project p) {
    String result = "";

    for (Iterator i = (p.getProjectLoader().getErrorCollector()).getUserFriendlyErrors(); i.hasNext(); ) {
      result += i.next() + StringUtil.NEWLINE + StringUtil.NEWLINE;
    }

    return result;
  }
}
