/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.commonIDE.ShortcutAction;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;


/**
 * @author Tonis Vaga
 */
public class StandaloneMenuBuilder extends MenuBuilder {
  private JPopupMenu menu;
  private JMenu sMenu;

  public StandaloneMenuBuilder(String name, char mnemonic, boolean submenu) {
    if (submenu) {
      this.sMenu = new JMenu(name);
      if (mnemonic != 0) {
        sMenu.setMnemonic(mnemonic);
      }
    } else {
      menu = new JPopupMenu();
    }
  }

  public Object getMenu() {
    if (sMenu != null) {
      return sMenu;
    }
    return menu;
  }

  public void addSeparator() {
    if (sMenu != null) {
      sMenu.addSeparator();
    } else {
      menu.addSeparator();
    }
  }

  public void addAction(Object action, boolean isEnabled) {

    JMenuItem item;

    if (action instanceof StandaloneAction) {

      StandaloneAction act = (StandaloneAction) action;
      item = new JMenuItem(act.getName());

      item.addActionListener(act);

      if (act.getAction() instanceof ShortcutAction) {
        final KeyStroke keyStroke = ((ShortcutAction) act.getAction()).
            getKeyStroke();
        if (keyStroke != null) {
          item.setAccelerator(keyStroke);
        }
      }
    } else if (action instanceof IdeAction) {
      item = new JMenuItem(((IdeAction) action).getName());
      item.addActionListener(new StandaloneIdeAction((IdeAction) action));
    } else {
      Assert.must(false, "" + action.getClass());
      return;
    }

    if (!isEnabled) {
      item.setEnabled(false);
    }
    if (sMenu != null) {
      sMenu.add(item);
    } else {
      menu.add(item);
    }
  }

  public void addSubMenu(Object subMenu) {
    if (this.sMenu != null) {
      this.sMenu.add((JMenu) subMenu);
    } else {
      menu.add((JMenu) subMenu);
    }
  }
}
