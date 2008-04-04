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
import net.sf.refactorit.source.edit.Editor;


/**
 *
 * @author vadim
 */
public abstract class UpMemberConflict extends Conflict {
  private BinMember upMember;

  public UpMemberConflict(BinMember upMember) {
    this.upMember = upMember;
  }

  public abstract void resolve();

  public abstract Editor[] getEditors();

  public abstract ConflictType getType();

  public abstract boolean isResolvable();

  public abstract String getDescription();

  public abstract int getSeverity();

  public BinMember getUpMember() {
    return upMember;
  }

}
