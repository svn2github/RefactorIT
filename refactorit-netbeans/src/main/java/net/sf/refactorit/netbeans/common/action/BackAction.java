/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.action;


import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.BackInfo;
import net.sf.refactorit.netbeans.common.ElementInfo;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.gotomodule.actions.GotoAction;

import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

import javax.swing.JEditorPane;

import java.awt.event.ActionEvent;
import java.util.ArrayList;


public class BackAction extends SystemAction {
  private static ArrayList list = new ArrayList();

  public boolean isEnabled() {
    return true;
  }

  protected void initialize() {
    super.initialize();
    RefactorItActions.staticInit();
  }

  public void actionPerformed(ActionEvent ev) {
    // FIXME: ugly delegation
    net.sf.refactorit.ui.module.BackAction.backCursor(
        IDEController.getInstance().createProjectContext());
//  	try {
//  		backCaretPosition();
//  	} catch( RuntimeException ignore ) {
//  	} finally {
//  		if ( list.isEmpty() ) return;
//  		list.remove( list.size() - 1 );
//  	}
  }

  public String getName() {
    return "Back";
  }

  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }

  /**
   * Convinience function that returns a path to the icon. To be used
   * in ancestor classes.
   **/
  protected String iconResource() {
    String iconFileName = "back_action.gif";

    return StringUtil.replace(UIResources.class.getPackage().getName(), '.', '/') +
        "/images/" + iconFileName;
  }

  public static void saveCaretPosition(JEditorPane pane, int position) {
    //System.out.println( "*** saveCaretPosition: " + position );
    BackInfo back = new BackInfo(pane, position);
    list.add(back);
  }

  protected static void backCaretPosition() {
    if (list.isEmpty()) {
      return;
    }

    BackInfo back = (BackInfo) list.get(list.size() - 1);
    if (back.editorPane != null) {
      back.editorPane.setCaretPosition(back.positionInPane);
      back.editorPane.grabFocus();
      back.editorPane.revalidate();
      back.editorPane.repaint();
    }
  }

  public static void notifyWillRun(RefactorItAction action, ElementInfo element,
      int line) {
    if (action.getKey().equals(GotoAction.KEY)) {
      net.sf.refactorit.ui.module.BackAction.addRecord(element.getCompilationUnit(),
          line);
      //backCaretPosition();
    }
  }
}
