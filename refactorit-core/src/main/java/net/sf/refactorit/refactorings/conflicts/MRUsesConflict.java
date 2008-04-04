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
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.DialogManager;

import java.util.List;


/**
 *
 * @author vadim
 */
public class MRUsesConflict extends MRUpDownMemberConflict {
  public static final ConflictType conflictType = ConflictType.USES;
  private ConflictResolver resolver;

  public MRUsesConflict(ConflictResolver resolver, BinMember upMember,
      List downMembers) {
    super(upMember, downMembers);

    this.resolver = resolver;
  }

  public ConflictType getType() {
    return MRUsesConflict.conflictType;
  }

  public String getDescription() {
    return BinFormatter.format(getUpMember())
        + " must have access to the following members";
  }

  public void resolve() {
    ConflictResolution resolution =
        DialogManager.getInstance().getResultFromResolutionDialog(
        getPossibleResolutions());

    if (resolution != null) {
      setResolution(resolution);
      resolution.runResolution(resolver);
    }
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
