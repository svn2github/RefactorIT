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
import net.sf.refactorit.refactorings.conflicts.resolution.DeleteOtherImplementersResolution;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.ui.DialogManager;

import java.util.List;


/**
 * Conflict indicating that there are other members with the same signature
 * in other subclasses (used when member is pulled to a superclass). 
 */
public class DeleteOtherImplementersConflict extends UpDownMemberConflict {
  private static final ConflictType conflictType = ConflictType.DELETE_IMPLEMENTATIONS_IN_SUBCLASSES;

  private ConflictResolver resolver;

  boolean isResolved = false;
  
  public DeleteOtherImplementersConflict(ConflictResolver resolver,
      BinMember upMember, List implementers) {
    super(upMember, implementers);

    this.resolver = resolver;
  }

  public ConflictType getType() {
    return DeleteOtherImplementersConflict.conflictType;
  }

  public String getDescription() {
    return "Delete following similar members from other subclasses of the target class?"; 
  }

  public boolean isResolvable() {
    return true;
  }

  public void resolve() {
    int questResult = DialogManager.getInstance().getResultFromQuestionDialog(
        "Delete other implementations?",
        "Delete similar members from other subclasses of the target class?");
    if (questResult != DialogManager.NO_BUTTON) {
      ConflictResolution resolution = new DeleteOtherImplementersResolution(getDownMembers());
      setResolution(resolution);
      resolution.runResolution(resolver);
    } 
    isResolved = true;
  }

  public boolean isResolved() {
    return isResolved;
  }
  

  public int getSeverity() {
    return RefactoringStatus.QUESTION;
  }

  public Editor[] getEditors() {
    ConflictResolution resolution = getResolution();
    if (resolution != null) {
      return resolution.getEditors(resolver);
    } else {
      return new Editor[0];
    }
  }

}
