/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import com.borland.primetime.actions.ActionGroup;

import javax.swing.Action;
import javax.swing.ImageIcon;

import net.sf.refactorit.commonIDE.MenuBuilder;


/**
 *
 *
 * @author Tonis Vaga
 */
public class JBMenuBuilder extends MenuBuilder {
  private ActionGroup menu;

  public JBMenuBuilder(String name, char mnemonic, String icon) {
    if (icon != null) {
      ImageIcon iconImg = getIcon(icon);
      menu = new ActionGroup(name, mnemonic, name, iconImg, true); // HACK
    } else {
      menu = new ActionGroup(name, mnemonic, true);
    }
  }

  public void addAction(final Object action, boolean isEnabled) {
    JBAction jbAction = (JBAction) action;

    Action result = null;

    // doesn't work

    if (!isEnabled) {
      //result=(Action)jbAction.clone();
      result = new JBEmptyAction(jbAction.getName());

//    	result = new AbstractAction(jbAction.getName()) {
//    		public void actionPerformed(ActionEvent e) {
//    			; // doNothing
//    		}
//    	};
      result.setEnabled(false);
    } else {
      result = (Action) jbAction;
    }

    if (result instanceof JBIdeAction) {
      ((JBIdeAction) result).updateName();
    }
//   result=(Action)jbAction;

//    ideAction.setEnabled(isEnabled);
    menu.add(result);
  }

  public void addSeparator() {
    // FIXME
    ActionGroup group = new ActionGroup("");
    group.add(new ActionGroup());
    menu.add(group);
  }

  public void addSubMenu(Object subMenu) {
    this.menu.add((ActionGroup) subMenu);
  }

  public Object getMenu() {
    return menu;
  }
}
