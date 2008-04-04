/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.vfs;


import com.borland.jbuilder.node.JBProject;
import com.borland.jbuilder.paths.ProjectPathSet;
import com.borland.primetime.vfs.Url;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.options.Path;
import net.sf.refactorit.commonIDE.options.PathItem;
import net.sf.refactorit.jbuilder.RefactorItPropGroup;
import net.sf.refactorit.vfs.JavadocPath;


/**
 * @author Anton Safonov
 */
public class JBJavadocPath extends JavadocPath {
  private JBProject jbProject;

  public JBJavadocPath(JBProject jbProject) {
    this.jbProject = jbProject;
  }

  public String[] getElements() {
    jbProject.check(); // resync properties

    ProjectPathSet pathset = jbProject.getPaths();

    List result = new ArrayList();

    Url[] javadocPaths = pathset.getDocPath();
    for (int i = 0; i < javadocPaths.length; i++) {
      CollectionUtil.addNew(result, javadocPaths[i].getFullName());
    }
    if (result.size() == 0) {
      javadocPaths = pathset.getFullDocPath();
      for (int i = 0; i < javadocPaths.length; i++) {
        CollectionUtil.addNew(result, javadocPaths[i].getFullName());
      }
    }
    
    String specifiedSourcepath = RefactorItPropGroup.getProjectProperty(
        RefactorItPropGroup.SPECIFIED_JAVADOC, "");
    List specifiedJavadocs = new Path(specifiedSourcepath).toPathItems();
    for(Iterator it = specifiedJavadocs.iterator(); it.hasNext(); ) {
      CollectionUtil.addNew(result, ((PathItem)it.next()).toString());
    }
    

    return (String[]) result.toArray(new String[result.size()]);
  }
}
