/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.ui.module.ActionProxy;
import net.sf.refactorit.ui.module.RefactorItActionUtils;

import java.awt.event.ActionEvent;


/**
 * @author Tonis Vaga
 */
public class StandaloneIdeAction implements StandaloneAction {
  private IdeAction ideAction;
  public void actionPerformed(ActionEvent e) {
    RefactorItActionUtils.run(ideAction);
  }

  public String getName() {
    return ideAction.getName();
  }

  public ActionProxy getAction() {
    return ideAction;
  }

  public StandaloneIdeAction(IdeAction ideAction) {
    this.ideAction = ideAction;
  }
}
