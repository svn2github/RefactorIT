/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v4;


import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.utils.SwingUtil;

import org.openide.awt.JInlineMenu;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import java.awt.Font;

public class DynamicMenu4 extends JInlineMenu {
  {
    setFont(getFont().deriveFont(Font.BOLD));
  }
  
  public void addNotify() {
    updateContents();
    super.addNotify();
  }

  public void updateContents() {
    setMenuItems(SwingUtil.getMenuItems(RefactorItActions.createPresenterNoExceptions()));
  }
};
