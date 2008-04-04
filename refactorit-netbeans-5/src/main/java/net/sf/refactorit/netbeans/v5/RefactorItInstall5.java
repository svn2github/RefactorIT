/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v5;

import net.sf.refactorit.netbeans.common.ElementInfo;
import net.sf.refactorit.netbeans.common.NBController;
import net.sf.refactorit.netbeans.common.RefactorItInstall;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.netbeans.v5.vfs.NBSourceVersionState5;



public class RefactorItInstall5 extends RefactorItInstall {

  protected void initVersionSpecifics() {
    NBController.setVersionState(new NBControllerVersionState5());
    NBSource.setVersionState(new NBSourceVersionState5());
    ElementInfo.setVersionState(new ElementInfoVersionState5());
    RefactorItActions.setVersionState(new RefactorItActionsVersionState5());
  }
}
