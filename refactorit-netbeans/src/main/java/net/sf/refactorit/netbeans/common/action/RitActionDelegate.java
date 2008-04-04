/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.action;


import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;

import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

import java.awt.event.ActionEvent;


public abstract class RitActionDelegate extends SystemAction {

  protected abstract IdeAction getRitAction();

  public void actionPerformed(ActionEvent e) {
    RefactorItActionUtils.run(ritAction);
  }

  public String getName() {
    return ritAction.getName();
  }

  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }

  protected final IdeAction ritAction = getRitAction();
}
