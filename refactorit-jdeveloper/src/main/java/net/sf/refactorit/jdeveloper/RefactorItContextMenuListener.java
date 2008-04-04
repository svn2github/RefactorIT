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
import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.module.RunContext;
import oracle.ide.ContextMenu;
import oracle.ide.addin.Context;
import oracle.ide.addin.ContextMenuListener;

import javax.swing.JMenu;


/**
 *
 *
 * @author  Tanel
 */
public abstract class RefactorItContextMenuListener implements
    ContextMenuListener {

  /**
   * Default constructor. A singleton instance is created when the addin
   * is loaded at startup.
   */
  RefactorItContextMenuListener() {
  }

  /**
   * Installs this listener's menu items in the context menu.
   * This method is called when the context menu is assembled, just before it
   * is displayed.
   *
   * @param menu the context menu to be displayed.
   */
  public void poppingUp(ContextMenu menu) {
    if (!RefactorITLock.lock()) {
      return;
    }

    try {
      // FIXME: Do we need syncronize this?
//      System.out.println("tonisdebug: poppingUp sync");
      Context context = (menu == null) ? null : menu.getContext();
      if (context == null) {
        return;
      }

      MenuBuilder rMenuBuilder = MenuBuilder.createEmptyRefactorITMenu('R', true);

      rMenuBuilder.buildContextMenu(extractRunContext(context));

      menu.addSeparator();
      menu.add((JMenu) rMenuBuilder.getMenu());

      // Create the root Menu Item and add the common items into it.
//      if (separator == null) {
//        separator = new JSeparator();
//        commonMenuItems = OldMenuBuilder.getInstance().getCommonMenuItems();
//        rebuildCleanMenuItems =
//            OldMenuBuilder.getInstance().getRebuildCleanMenuItems();
//      }
//
//      JMenu refactorItMenu = Ide.getMenubar().createSubMenu("RefactorIT",
//          new Integer('R'));
//      refactorItMenu.setIcon(ResourceUtil.getIcon(this.getClass(),
//          "RefactorIt.gif"));
//      Font boldFont = refactorItMenu.getFont().deriveFont(Font.BOLD);
//      refactorItMenu.setFont(boldFont);
//
//      SwingUtil.addIntoMenu(refactorItMenu, commonMenuItems);
//      refactorItMenu.addSeparator();
//      SwingUtil.addIntoMenu(refactorItMenu, rebuildCleanMenuItems);
//
//      populateRefactorITMenu(refactorItMenu, context);
//
//      menu.add(separator);
//      menu.add(refactorItMenu);
    } finally {
      RefactorITLock.unlock();
    }
  }

  abstract RunContext extractRunContext(Context context);

//  protected abstract void populateRefactorITMenu(JMenu menu, Context context);

  /**
   * Deactivates context menu items.
   * This method is called when a context menu is closed.
   *
   * @param menu the context menu that is now displayed.
   */
  public void poppingDown(ContextMenu menu) {
  }

  /**
   * Invokes a UI element's default action.
   * This method is called when the user double-clicks on an element
   * that has a context menu, unless a previously installed listener's
   * <tt>handleDefaultAction</tt> has already handled the action and
   * returned <tt>true</tt>
   *
   * @param context the current context.
   * @return true if this listener handles the default action.
   */
  public boolean handleDefaultAction(Context context) {
    return false;
  }
}
