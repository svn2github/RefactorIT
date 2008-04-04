/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.conflicts;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.MoveMemberResolution;

import java.util.List;


/**
 *
 * @author vadim
 */
public class ConflictModel {
  private ConflictResolver resolver;

  public ConflictModel(ConflictResolver resolver) {
    this.resolver = resolver;
  }

  public RefactoringStatus getResolvedConflictsStatus(ConflictData data) {
    if (!data.resolvedConflictsExist()) {
      return null;
    }

    RefactoringStatus status = new RefactoringStatus();
    List conflicts = data.getConflicts();
    List downMembers = null;

    RefactoringStatus.Entry resolutionEntry = null;
    RefactoringStatus.Entry mainEntry = status.addEntry(data.getMember());

    for (int i = 0, max = conflicts.size(); i < max; i++) {
      Conflict conflict = (Conflict) conflicts.get(i);

      if (!conflict.isResolved()) {
        continue;
      }

      ConflictResolution conflictResolution = conflict.getResolution();
      if (conflictResolution == null) {
        continue;
      }
      
      if (conflictResolution instanceof MoveMemberResolution) {
        continue;
      }

      resolutionEntry = mainEntry.addSubEntry(conflictResolution.getDescription());

      downMembers = conflictResolution.getDownMembers();
      if (downMembers == null) {
        continue;
      }

      for (int j = 0, maxJ = downMembers.size(); j < maxJ; j++) {
        BinItem downItem = (BinItem) downMembers.get(j);
        resolutionEntry.addSubEntry(downItem);
      }
    }

    return (mainEntry.getSubEntries().size() > 0) ? status : null;
  }

  public RefactoringStatus getUnresolvedConflictsStatus(ConflictData data) {
    RefactoringStatus status = new RefactoringStatus();

    List conflicts = data.getConflicts();
    BinMember upMember = null;
    List downMembers = null;
    boolean isUnresolvableConflictsExist = data.unresolvableConflictsExist();

    RefactoringStatus.Entry conflictEntry = null;
    RefactoringStatus.Entry mainEntry = status.addEntry(data.getMember());

    for (int i = 0, max = conflicts.size(); i < max; i++) {
      Conflict conflict = (Conflict) conflicts.get(i);

      if (conflict.isResolved() || conflict.isObsolete() ||
          (isUnresolvableConflictsExist && conflict.isResolvable())) {
        continue;
      }

      if (conflict instanceof UpDownMemberConflict) {
        upMember = ((UpDownMemberConflict) conflict).getUpMember();
        downMembers = ((UpDownMemberConflict) conflict).getDownMembers();
      }

      mainEntry.setConflict(conflict);
      conflictEntry = mainEntry.addSubEntry(conflict.getDescription(),
          conflict.getSeverity(),
          upMember);
      conflictEntry.setConflict(conflict);

      if (downMembers == null) {
        continue;
      }

      for (int j = 0, maxJ = downMembers.size(); j < maxJ; j++) {
        BinItem downItem = (BinItem) downMembers.get(j);
        RefactoringStatus.Entry subEntry = conflictEntry.addSubEntry(downItem);
        subEntry.setConflict(conflict);
      }
    }

    return (status.getEntries().size() > 0) ? status : null;
  }
}
