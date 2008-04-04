/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;


import net.sf.refactorit.commonIDE.MenuBuilder;
import oracle.ide.Ide;
import oracle.ide.IdeAction;
import oracle.ide.controls.ToggleAction;

import javax.swing.Icon;
import javax.swing.JMenu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * @author Tonis Vaga
 */
public class JDevMenuBuilder extends MenuBuilder {
  JMenu menu;

//  private IdeAction debugShellAction;
//
//  private IdeAction helpAction;
//  private IdeAction aboutAction;
//  private IdeAction updateAction;
//  private IdeAction browserAction;
//  private IdeAction crossHtmlAction;
//  private IdeAction optionsAction;
//
//  private IdeAction rebuildAction;
//  private IdeAction cleanAction;
//
//  private IdeAction whereUsedAction;
//  private IdeAction renameAction;
//  private IdeAction infoAction;
//  private IdeAction goToDeclarationAction;

  /** Used for the menu items in the main menu that are always visible
   * -- that menu is not modified on the fly */

  //FIXME: refactor out this BinSelection stuff usage
  public static final String BINSELECTION_NECCESSITY =
      "RefactorIT_binSelection_neccessity";

  public static final int BINSELECTION_DONT_CARE = 0;
  public static final int BINSELECTION_NEEDED = 1;
  public static final int BINSELECTION_PROHIBITED = 2;

  private float sectionId = 0.1f;

  public JDevMenuBuilder(
      String name, char mnemonic, String icon, boolean subMenu
      ) {
    Icon iconImg = null;

    if (icon != null && !icon.equals("")) {
      iconImg = getIcon(icon);
    }

    if (subMenu) {
      menu = Ide.getMenubar().createSubMenu(name, new Integer(mnemonic));
    } else {
      try {
        Class menuConstants = Class.forName("oracle.ide.MenuConstants");
        Field field = menuConstants.getField("WEIGHT_TOOLS_MENU");
        float weight = field.getFloat(menuConstants) + 0.001F;
        Method method = Ide.getMenubar().getClass().getMethod("createMenu",
            new Class[] {String.class, Integer.class, Float.TYPE});
        menu = (JMenu) method.invoke(Ide.getMenubar(),
            new Object[] {name, new Integer(mnemonic), new Float(weight)});
      } catch (Error e) {
        menu = Ide.getMenubar().createMenu(name, new Integer(mnemonic));
      } catch (Exception e) {
        menu = Ide.getMenubar().createMenu(name, new Integer(mnemonic));
      }
    }

    if (iconImg != null) {
      menu.setIcon(iconImg);
    }

//
//    JMenu packageMenu = Ide.getMenubar().createSubMenu("Package", new Integer((int)'P'));
//    JMenu classMenu = Ide.getMenubar().createSubMenu("Class", new Integer((int)'C'));
//    JMenu methodMenu = Ide.getMenubar().createSubMenu("Method", new Integer((int)'M'));
//    JMenu fieldMenu = Ide.getMenubar().createSubMenu("Field", new Integer((int)'F'));
//
//    // add menuitems into corresponding submenus
//    SwingUtil.addIntoMenu(packageMenu, getPackageMenuItems());
//    SwingUtil.addIntoMenu(classMenu, getClassMenuItems());
//    SwingUtil.addIntoMenu(methodMenu, getMethodMenuItems());
//    SwingUtil.addIntoMenu(fieldMenu, getFieldMenuItems());

  }

  public Object getMenu() {
    return menu;
  }

  public void addSeparator() {
    menu.addSeparator(); // this is for legacy menus, ignored in new JDev
    this.sectionId += 0.1f;
  }

  public void addAction(Object action, boolean isEnabled) {
    IdeAction ideAction = (IdeAction) action;
    ideAction.setEnabled(isEnabled);
    AbstractionUtils.add(Ide.getMenubar().createMenuItem((ToggleAction)
        ideAction), menu, sectionId);
  }

  public void addSubMenu(Object subMenu) {
    AbstractionUtils.add((JMenu) subMenu, menu, sectionId);
  }
}
