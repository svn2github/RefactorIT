/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v5;


import net.sf.refactorit.netbeans.common.RefactorItActionsVersionState;

import org.netbeans.api.javahelp.Help;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * @author risto
 */
public class RefactorItActionsVersionState5 implements RefactorItActionsVersionState {

  public void displayHelpTopic(String topicId) {
    Object help = Lookup.getDefault().lookup(Help.class);
    if (help != null) {
      ((Help) help).showHelp(new HelpCtx(topicId));
    }
  }

}
