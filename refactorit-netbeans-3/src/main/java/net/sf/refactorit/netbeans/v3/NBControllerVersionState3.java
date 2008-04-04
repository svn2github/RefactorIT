/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v3;

import net.sf.refactorit.netbeans.common.NBControllerVersionState;
import net.sf.refactorit.netbeans.common.VersionSpecific;

import org.openide.TopManager;


/**
 * @author Juri Reinsalu
 */
public class NBControllerVersionState3 implements NBControllerVersionState {

  public Object getActiveProjectFromIDE() {
    return VersionSpecific.getInstance().getCurrentProject();
  }

  public void beginTransaction() {
  }

  public void endTransaction() {
  }

  public Object[] getIdeProjects() {
    return new Object[] {TopManager.getDefault().getPlaces().nodes().projectDesktop()};
  }

}
