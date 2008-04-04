/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movemember;

import net.sf.refactorit.ui.treetable.BinTreeTableNode;


/**
 * Selectable node.
 *
 * @author Anton Safonov, Vadim Hahhulin
 */
public class MoveMemberNode extends BinTreeTableNode {
  private boolean convertPrivates = false;

  public MoveMemberNode(Object bin) {
    super(bin, false);
  }

  public boolean isConvertPrivates() {
    return convertPrivates;
  }

  public void setConvertPrivates(boolean convertPrivates) {
    this.convertPrivates = convertPrivates;
  }
}
