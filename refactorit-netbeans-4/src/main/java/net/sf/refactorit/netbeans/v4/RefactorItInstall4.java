/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v4;

import net.sf.refactorit.netbeans.common.ElementInfo;
import net.sf.refactorit.netbeans.common.NBController;
import net.sf.refactorit.netbeans.common.RefactorItInstall;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.netbeans.v4.vfs.NBSourceVersionState4;



public class RefactorItInstall4 extends RefactorItInstall {

  protected void initVersionSpecifics() {
    NBController.setVersionState(new NBControllerVersionState4());
    NBSource.setVersionState(new NBSourceVersionState4());
    ElementInfo.setVersionState(new ElementInfoVersionState4());
    RefactorItActions.setVersionState(new RefactorItActionsVersionState4());
  }
}
