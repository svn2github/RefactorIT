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
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.MoveMemberResolution;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.DialogManager;

import javax.swing.JOptionPane;

import java.util.List;


/**
 *
 * @author vadim
 */
public class MRUsedByConflict extends MRUpDownMemberConflict {
  private static final ConflictType conflictType = ConflictType.USED_BY;
  private ConflictResolver resolver;

  public MRUsedByConflict(ConflictResolver resolver, BinMember upMember,
      List downMembers) {
    super(upMember, downMembers);

    this.resolver = resolver;
  }

  public ConflictType getType() {
    return MRUsedByConflict.conflictType;
  }

  public String getDescription() {
    return "The following members must have access to " +
        BinFormatter.format(getUpMember());
  }

  public void resolve() {
    ConflictResolution resolution = null;
    boolean exit;
    do {
      exit = true;
      resolution = DialogManager.getInstance().getResultFromResolutionDialog(
          getPossibleResolutions());

      if (resolution instanceof MoveMemberResolution) {
        if (isMethodMainGoingToMove(getDownMembers())) {
          int questResult =
              DialogManager.getInstance().getResultFromQuestionDialog(
              "Move method main?",
              "Do you really want to move method main also?");

          if (questResult == JOptionPane.NO_OPTION) {
            exit = false;
          }
        }
      }
    } while (!exit);

    if (resolution != null) {
      setResolution(resolution);
      resolution.runResolution(resolver);
    }
  }

  private boolean isMethodMainGoingToMove(List downMembers) {
    for (int i = 0, max = downMembers.size(); i < max; i++) {
      BinMember member = (BinMember) downMembers.get(i);
      if ((member instanceof BinMethod) && ((BinMethod) member).isMain()) {
        return true;
      }
    }

    return false;
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
