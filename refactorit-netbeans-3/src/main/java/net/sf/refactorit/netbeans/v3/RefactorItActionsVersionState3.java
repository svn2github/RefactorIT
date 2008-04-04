/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v3;


import net.sf.refactorit.netbeans.common.RefactorItActionsVersionState;

import org.openide.TopManager;
import org.openide.util.HelpCtx;

/**
 * @author risto
 */
public class RefactorItActionsVersionState3 implements RefactorItActionsVersionState {

  public void displayHelpTopic(String topicId) {
    TopManager.getDefault().showHelp(new HelpCtx(topicId));
  }

}
