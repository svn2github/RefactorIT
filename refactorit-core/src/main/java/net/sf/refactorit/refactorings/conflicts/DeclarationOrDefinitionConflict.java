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


/**
 *
 * @author vadim
 */
public class DeclarationOrDefinitionConflict extends MRUpMemberConflict {
  private static final ConflictType conflictType = ConflictType.
      DECLARATION_OR_DEFINITION;

  private ConflictResolver resolver;

  public DeclarationOrDefinitionConflict(ConflictResolver resolver,
      BinMember upMember) {
    super(upMember);

    this.resolver = resolver;
  }

  public ConflictType getType() {
    return conflictType;
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

  public String getDescription() {
    return ("Do you want to move declaration or definition of " +
        BinFormatter.format(getUpMember()) + " ?");
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
