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
import net.sf.refactorit.refactorings.conflicts.resolution.CreateDeclarationResolution;
import net.sf.refactorit.source.edit.Editor;


/**
 *
 * @author vadim
 */
public class CreateOnlyDeclarationConflict extends UpMemberConflict {
  private static final ConflictType conflictType = ConflictType.
      CREATE_ONLY_DECLARATION;

  private ConflictResolver resolver;

  public CreateOnlyDeclarationConflict(ConflictResolver resolver,
      BinMember upMember) {
    super(upMember);

    this.resolver = resolver;
    setResolution(new CreateDeclarationResolution(upMember));

  }

  public ConflictType getType() {
    return CreateOnlyDeclarationConflict.conflictType;
  }

  public String getDescription() {
    return "";
  }

  public void resolve() {
    getResolution().runResolution(resolver);
  }

  public Editor[] getEditors() {
    return getResolution().getEditors(resolver);
  }

  public void setResolution(ConflictResolution resolution) {
    super.setResolution(resolution);
    resolve();
  }

  public int getSeverity() {
    return RefactoringStatus.UNDEFINED;
  }

  public boolean isResolvable() {
    return true;
  }
}
