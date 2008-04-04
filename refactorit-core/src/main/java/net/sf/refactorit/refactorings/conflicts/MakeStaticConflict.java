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
import net.sf.refactorit.refactorings.conflicts.resolution.MakeStaticResolution;
import net.sf.refactorit.source.edit.Editor;

import java.util.HashMap;
import java.util.List;



/**
 *
 * @author vadim
 */
public class MakeStaticConflict extends UpDownMemberConflict {
  private static final ConflictType conflictType = ConflictType.MAKE_STATIC;

  private ConflictResolver resolver;

  public MakeStaticConflict(ConflictResolver resolver,
      BinMember upMember, List downMembers) {
    this(resolver, upMember, downMembers, new HashMap());

  }

  public MakeStaticConflict(ConflictResolver resolver,
      BinMember upMember, List downMembers, HashMap extraImports) {
    super(upMember, downMembers);

    setResolution(new MakeStaticResolution(upMember,
        downMembers, extraImports));

    this.resolver = resolver;
  }

  public int getSeverity() {
    return RefactoringStatus.WARNING;
  }

  public String getDescription() {
    return getResolution().getDescription();
  }

  public ConflictType getType() {
    return MakeStaticConflict.conflictType;
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
}
