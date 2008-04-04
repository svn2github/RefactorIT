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
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.List;


/**
 *
 * @author vadim
 */
public class WeakAccessConflict extends UpDownMemberConflict {
  private ConflictType conflictType;
  private ConflictResolver resolver;

  public WeakAccessConflict(ConflictResolver resolver,
      ConflictType conflictType,
      BinMember upMember, List downMembers, ConflictResolution resolution) {
    super(upMember, downMembers);

    this.conflictType = conflictType;
    this.resolver = resolver;
    setResolution(resolution);
  }

  public ConflictType getType() {
    return conflictType;
  }

  public int getSeverity() {
    return RefactoringStatus.WARNING;
  }

  public String getDescription() {
    if (conflictType == ConflictType.NOT_PUBLIC_FOR_INTERFACE) {
      return "The access modifier of " + BinFormatter.format(getUpMember()) +
          " must be changed to public";
    } else if (conflictType == ConflictType.WEAK_ACCESS_FOR_ABSTRACT) {
      return "The access modifier of " + BinFormatter.format(getUpMember()) +
          " must become stronger";
    } else if (conflictType == ConflictType.UNMOVABLE_CANNOT_ACCESS) {
      return "The access modifier of " + BinFormatter.format(getUpMember()) +
          " must become stronger";
    } else if (conflictType == ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED) {
      return
          "The access modifier of the following members must become stronger";
    } else {
      return "Unknown conflict";
    }
  }

  public boolean isResolvable() {
    return true;
  }

  public Editor[] getEditors() {
    return getResolution().getEditors(resolver);
  }

  public void resolve() {
    ConflictResolution resolution = getResolution();
    //ConflictResolution resolution=DialogManager.getInstance().getResultFromResolutionDialog(
//        CollectionUtil.singletonArrayList(getResolution()));

    if (resolution != null) {
      resolution.runResolution(resolver);
    }
  }

  public void setResolution(ConflictResolution resolution) {
    super.setResolution(resolution);
//    resolve(); // resolve automatically
  }
}
