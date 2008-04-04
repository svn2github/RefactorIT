/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v5;


import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.utils.SwingUtil;

import org.openide.awt.DynamicMenuContent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;


public class DynamicMenu5 extends JMenuItem implements DynamicMenuContent {

  public JComponent[] getMenuPresenters() {
    return synchMenuPresenters(new JComponent[0]);
  }

  public JComponent[] synchMenuPresenters(JComponent[] items) {
    return SwingUtil.getMenuItems(RefactorItActions.createPresenterNoExceptions());
  }
}
