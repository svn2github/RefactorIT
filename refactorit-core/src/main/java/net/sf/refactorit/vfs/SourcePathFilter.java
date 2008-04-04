/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;

import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.options.GlobalOptions;



public final class SourcePathFilter {
  private String[] versionControlDirs;

  public SourcePathFilter() {
    initialize();
  }

  public final void initialize() {
    String dirList = GlobalOptions.getOption("version.control.dir.list");
    versionControlDirs = StringUtil.split(dirList, ";");
  }

  public final boolean acceptDirectoryByName(String name) {
    for (int i = 0; i < versionControlDirs.length; ++i) {
      if (versionControlDirs[i].equalsIgnoreCase(name)) {
        return false;
      }
    }

    return true;
  }
}
