/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import net.sf.refactorit.options.GlobalOptions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

public class RefactorItActionsToplevel extends SystemAction implements
    Presenter.Menu, Presenter.Popup {
  //private RefactorItActions refactoryActions = new RefactorItActions();
  
  private JMenuItem inlineMenu = VersionSpecific.getInstance().createMenuItem(); 

  public String getName() {
    return GlobalOptions.REFACTORIT_NAME;
  }

  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    //refactoryActions.actionPerformed(actionEvent);
  }

  public JMenuItem getMenuPresenter() {
    return inlineMenu; 
  }

  public JMenuItem getPopupPresenter() {
    return inlineMenu;
  }
}
