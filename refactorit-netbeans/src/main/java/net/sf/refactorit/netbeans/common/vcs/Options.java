/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vcs;

import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.VersionSpecific;
import net.sf.refactorit.options.GlobalOptions;


/**
 * @author risto
 */
public class Options {

  public static boolean vcsEnabled() {
    return VersionSpecific.getInstance().isVcsEnabled();
  }

  static boolean versionControlSetToQuietMode() {
    if(RefactorItActions.isNetBeansFour()) {
      return "false".equals(GlobalOptions.getOption("version.control.verbose", "false"));
    } else {
      return true;
    }
  }

}
