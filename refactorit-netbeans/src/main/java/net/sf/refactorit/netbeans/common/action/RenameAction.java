/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.action;

public class RenameAction extends HighLevelMenuAction {
  public String getName() {
    return "[R] Rename";
  }

  public String getActionKey() {
    return net.sf.refactorit.ui.refactoring.rename.RenameAction.KEY;
  }

  protected String iconResource() {
    return getIconResource("rename_action.gif");
  }

}
