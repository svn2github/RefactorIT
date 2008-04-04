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
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.List;



/**
 *
 * @author vadim
 */
public class OtherImplementersExistConflict extends UpDownMemberConflict {
  private static final ConflictType conflictType = ConflictType.
      IMPLEMENTATION_NEEDED;

  private ConflictResolver resolver;

  public OtherImplementersExistConflict(ConflictResolver resolver,
      BinMember upMember, List implementers) {
    super(upMember, implementers);

    this.resolver = resolver;
  }

  public ConflictType getType() {
    return OtherImplementersExistConflict.conflictType;
  }

  public String getDescription() {
    return "Implementation of " + BinFormatter.format(getUpMember())
        + " must be added" +
        " into the following types";
  }

  public boolean isResolvable() {
    return true;
  }

  public void resolve() {
    getResolution().runResolution(resolver);
  }

  public int getSeverity() {
    return RefactoringStatus.INFO;
  }

  public Editor[] getEditors() {
    return getResolution().getEditors(resolver);
  }
}
