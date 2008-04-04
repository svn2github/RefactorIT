/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import com.borland.primetime.editor.EditorAction;
import com.borland.primetime.editor.EditorPane;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.StructureView;
import com.borland.primetime.viewer.TextView;

import javax.swing.Icon;
import javax.swing.JTree;

import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.module.ActionProxy;
import net.sf.refactorit.ui.module.RefactorItAction;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;


public class JBRefactoritAction extends EditorAction implements JBAction,
    Cloneable {
  private RefactorItAction action;
  private Icon icon;

  public JBRefactoritAction(String name, Icon icon) {
    super(name);
    this.icon = icon;

    putValue("ActionGroup", "RefactorIT");
    if (icon != null) {
      putValue(EditorAction.SMALL_ICON, icon);

    }
    putValue(EditorAction.SHORT_DESCRIPTION, name);
    putValue(EditorAction.LONG_DESCRIPTION, name);
    putValue(EditorAction.NAME, name);

  }

  public JBRefactoritAction(RefactorItAction actionParam, Icon icon) {
    this(actionParam.getName(), icon);

    this.action = actionParam;
    //		super(action,icon);
  }

  public String getName() {
    return action.getName();
  }


  public void actionPerformed(ActionEvent e) {
    String key = action.getKey();

    if (!RefactorITLock.lock()) {
      return;
    }

    try {
      Browser.getActiveBrowser().dispatchEvent(new WindowEvent(
          Browser.getActiveBrowser(), WindowEvent.WINDOW_ACTIVATED));

      StructureView sv = Browser.getActiveBrowser().getStructureView();
      EditorPane pane = getEditorTarget(e);

      if (pane != null && pane.hasFocus()) {
        if (sv.hasFocusOwner() || e.getSource() instanceof JTree) {
          RefactorItActions.performAction(pane, sv, key);
        } else {
          RefactorItActions.performAction(pane, null, key);
        }
      } else {
        if (sv.hasFocusOwner() || e.getSource() instanceof JTree) {
          RefactorItActions.performAction(null, sv, key);
        } else if (pane != null && e.getSource() instanceof TextView) {
          RefactorItActions.performAction(pane, null, key);
          // right click in source but no focus change
        } else {
          RefactorItActions.performAction(null, null, key);
        }
      }
    } finally {
      RefactorITLock.unlock();
    }
  }

  public Object clone() {
    return new JBRefactoritAction(action, icon);
  }

  public void setAction(ActionProxy action) {
    this.action = (RefactorItAction) action;
  }

  /*
   * @see net.sf.refactorit.jbuilder.JBAction#getAction()
   */
  public ActionProxy getAction() {
    return action;
  }
}
