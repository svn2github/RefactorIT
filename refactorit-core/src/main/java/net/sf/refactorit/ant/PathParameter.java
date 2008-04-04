/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ant;


import net.sf.refactorit.common.util.StringUtil;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;


class PathParameter {
  Path p = null;

  public void set(Path p) {
    if (this.p == null) {
      this.p = p;
    } else {
      this.p.append(p);
    }
  }

  public Path create(Project antProject) {
    if (p == null) {
      p = new Path(antProject);
    }

    return p.createPath();
  }

  public void setRef(Reference r, Project antProject) {
    create(antProject).setRefid(r);
  }

  public String getStringForm() {
    if (p == null) {
      return "";
    }

    StringBuffer result = new StringBuffer();
    String[] items = p.list();
    for (int i = 0; i < items.length; i++) {
      if (i > 0) {
        result.append(StringUtil.PATH_SEPARATOR);
      }
      result.append(items[i]);
    }
    return result.toString();
  }
}
