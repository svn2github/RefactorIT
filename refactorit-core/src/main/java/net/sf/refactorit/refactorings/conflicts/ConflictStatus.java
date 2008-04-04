/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.conflicts;


import net.sf.refactorit.classmodel.BinMember;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class ConflictStatus {
  Collection conflicts;
  BinMember member;

  /**
   * @param conflicts
   * @param member
   */
  public ConflictStatus(BinMember member, Collection conflicts) {
    this.conflicts = conflicts;
    this.member = member;
  }

  public boolean unresolvableConflictsExist() {
    for (Iterator i = conflicts.iterator(); i.hasNext(); ) {
      if (!((Conflict) i.next()).isResolvable()) {
        return true;
      }
    }
    return false;
  }

  public boolean unresolvedConflictsExist() {
    for (Iterator i = conflicts.iterator(); i.hasNext(); ) {
      if (!((Conflict) i.next()).isResolved()) {
        return true;
      }
    }
    return false;
  }

  public boolean resolvedConflictsExist() {
    for (Iterator i = conflicts.iterator(); i.hasNext(); ) {
      if (((Conflict) i.next()).isResolved()) {
        return true;
      }
    }
    return false;
  }

  public boolean isNoConflicts() {
    return (conflicts.size() == 0);
  }

  public List getConflicts() {
    return new ArrayList(conflicts);
  }

  public BinMember getMember() {
    //FIXME: implement this
    throw new java.lang.UnsupportedOperationException(
        "Method getMember() not implemented");
  }
}
