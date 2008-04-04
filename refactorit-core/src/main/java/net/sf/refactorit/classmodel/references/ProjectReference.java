/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * @author Arseni Grigorjev
 */
public class ProjectReference extends CacheableReference {
  private static final HashMap referencePool = new HashMap();

  private final Object id;

  private ProjectReference(final Project project) {
    super(project, project);
    id = project.getName();
  }

  public Object findItem(Project project){
    final Set projects = IDEController.getInstance().getWorkspace()
        .getProjects().getValueSet();
    for (Iterator it = projects.iterator(); it.hasNext();){
      Project proj = (Project) it.next();
      if (proj.getName().equals(id)){
        return proj;
      }
    }
    return null;
  }

  public static BinItemReference createAdvancedReference(Project project){
    BinItemReference result
        = (BinItemReference) referencePool.get(project.getName());
    if (result == null){
      result = new ProjectReference(project);
      referencePool.put(project.getName(), result);
    }
    return result;
  }

  public String toString() {
    return super.toString() + ", id=" + id;
  }
}
