/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.BrowserAction;

import javax.swing.Icon;

import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.ui.module.ActionProxy;
import net.sf.refactorit.ui.module.RefactorItActionUtils;


class JBIdeAction extends BrowserAction implements JBAction, Cloneable {
  private IdeAction action;
  private final Icon icon;
  public JBIdeAction(IdeAction ideAction, Icon icon) {

    super(ideAction.getName(),
        ideAction.getMnemonic(),
        ideAction.getName(),
        icon);

    this.icon = icon;
    this.action = ideAction;
    String name = ideAction.getName();

    if (ideAction instanceof ShortcutAction) {
      putValue("ActionGroup", "RefactorIT");
      if (icon != null) {
        putValue(BrowserAction.SMALL_ICON, icon);

      }
      putValue(BrowserAction.SHORT_DESCRIPTION, name);
      putValue(BrowserAction.LONG_DESCRIPTION, name);
      putValue(BrowserAction.NAME, name);
    }
  }

  public void updateName() {
    String name = getName();
    setShortText(name);
    setLongText(name);
  }

  public Object clone() {
    JBIdeAction result = null;
    result = new JBIdeAction(this.action, icon);
    return result;
  }

  public String toString() {
    return getName();
  }

  public String getName() {
    return action.getName();
  }

  public String getShortText() {
    return getName();
  }

  public void actionPerformed(Browser browser) {
    RefactorItActionUtils.run(action);
  }

  public void setAction(ActionProxy action) {
    this.action = (IdeAction) action;

  }

  public ActionProxy getAction() {
    return action;
  }

  public String getLongText() {
    return getShortText();
  }
}
