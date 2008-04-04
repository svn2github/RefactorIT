/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;


import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Anton Safonov
 */
public final class InvocationTreeTableNode extends BinTreeTableNode {
  private InvocationData invocationData = null;

  /**
   * Sometimes there are many invocations on one line && WhereModel can't
   * contain multiple nodes of the same lime; then this list contains more
   * than 1 item.
   */
  private List allInvocationData = null;

  public InvocationTreeTableNode(final Object o) {
    super(o);
  }

  public final InvocationData getInvocationData() {
    return invocationData;
  }

  public final void setInvocationData(final InvocationData data) {
    this.invocationData = data;
    addInvocationData(data);
  }

  public final void addInvocationData(final InvocationData data) {
    getAllInvocationData().add(data);
  }

  private List getAllInvocationData() {
    if (this.allInvocationData == null) {
      this.allInvocationData = new ArrayList(1);
    }
    return this.allInvocationData;
  }
}
