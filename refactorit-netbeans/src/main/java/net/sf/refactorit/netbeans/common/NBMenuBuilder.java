/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import javax.swing.ImageIcon;
import javax.swing.JMenu;

import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.module.RunContext;

import java.awt.Font;


/**
 *
 *
 * @author Tonis Vaga
 */
public class NBMenuBuilder extends MenuBuilder {
  JMenu menu;

  public NBMenuBuilder(String name, char mnemonic, String iconKey) {
    menu = new JMenu(name);
    menu.setFont(menu.getFont().deriveFont(Font.BOLD));
    menu.setMnemonic(mnemonic);
    if (iconKey != null) {
      menu.setIcon(getIcon(iconKey));
    } else {
      // Makes iconless items line up with others
      menu.setIcon(new ImageIcon(ResourceUtil.getImage(
          UIResources.class, "blank_menuitem_icon.gif")));
    }
  }

  public void addSeparator() {
    menu.addSeparator();
  }

  public void addAction(Object action, boolean isEnabled) {
    menu.add(((NBAction) action).createMenuItem(isEnabled));
  }

  public void addSubMenu(Object subMenu) {
    menu.add((JMenu) subMenu);
  }

  public Object getMenu() {
    return menu;
  }

//  public void buildToplevelMenu() {
//    super.addCommonIdeActions();
//    addSeparator();
//    super.buildToplevelMenu();
//  }

  public void buildContextMenu(RunContext runContext) {
    MenuBuilder settingsMenu = new NBMenuBuilder("Help & Settings", 'h', null);
    settingsMenu.addCommonIdeActions();
    addSubMenu(settingsMenu.getMenu());
    addSeparator();
    super.buildContextMenu(runContext);
  }
}
