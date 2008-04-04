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
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.MoveMemberResolution;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.format.BinFormatter;


/**
 *
 * @author vadim
 */
public class ChangedFunctionalityConflict extends UpMemberConflict {
  private static final ConflictType conflictType = ConflictType.
      CHANGED_FUNCTIONALITY;

  private ConflictResolver resolver;

  public ChangedFunctionalityConflict(ConflictResolver resolver,
      BinMember member, ConflictResolution resolution) {
    super(member);

    this.resolver = resolver;
    setResolution(resolution);
  }

  public int getSeverity() {
    return RefactoringStatus.WARNING;
  }

  public String getDescription() {
    return "If you move " + BinFormatter.format(getUpMember()) +
        " it can change the functionality of your program!";
  }

  public ConflictType getType() {
    return ChangedFunctionalityConflict.conflictType;
  }

  public Editor[] getEditors() {
    return getResolution().getEditors(resolver);
  }

  public boolean isResolvable() {
    return true;
  }

  public void resolve() {
    getResolution().runResolution(resolver);
  }

  public boolean isMoveField() {
    return ((MoveMemberResolution) getResolution()).isMoveField();
  }
}
